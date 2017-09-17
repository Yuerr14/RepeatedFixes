// directory.cc 
//	Routines to manage a directory of file names.
//
//	The directory is a table of fixed length entries; each
//	entry represents a single file, and contains the file name,
//	and the location of the file header on disk.  The fixed size
//	of each directory entry means that we have the restriction
//	of a fixed maximum size for file names.
//
//	The constructor initializes an empty directory of a certain size;
//	we use ReadFrom/WriteBack to fetch the contents of the directory
//	from disk, and to write back any modifications back to disk.
//
//	Also, this implementation has the restriction that the size
//	of the directory cannot expand.  In other words, once all the
//	entries in the directory are used, no more files can be created.
//	Fixing this is one of the parts to the assignment.
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// All rights reserved.  See copyright.h for copyright notice and limitation 
// of liability and disclaimer of warranty provisions.

#include "copyright.h"
#include "utility.h"
#include "filehdr.h"
#include "directory.h"
#include <cstring>

#define meSector 0
#define paSector 1

char* getNameFromDictorySector(int sector)
{
    if(sector == 1)
    {
        char* _ret = new char[2];
        _ret[0] = '/';
        _ret[1] = '\0';
        return _ret;
    }
    OpenFile* directoryFile = new OpenFile(sector);
    Directory *directory;
    directory = new Directory(12);
    directory->FetchFrom(directoryFile);
    char* name = directory->FindEntryName(paSector);
    int fatherSector = directory->Find(name);
    char* fatherName = getNameFromDictorySector(fatherSector);
    delete directoryFile;
    delete directory;
    char* _ret = new char[strlen(fatherName) + 1 + strlen(name)];
    strcpy(_ret, fatherName);
    strcat(_ret, name);
    strcat(_ret, "/");
    delete [] fatherName;
    return _ret;
}

char*
DirectoryEntry::getName(int fatherSector = 0)
{
    char* name = new char[nameSize+1];
    OpenFile *filenameFile = new OpenFile(2);
    filenameFile->Seek(nameIndex);
    filenameFile->Read(name, nameSize);
    delete filenameFile;
    name[nameSize] = '\0';


    if(fatherSector > 0)
    {
        char* fatherName = getNameFromDictorySector(fatherSector);
        char* _ret = new char[strlen(fatherName) + 1 + strlen(name)];
        strcpy(_ret, fatherName);
        strcat(_ret, name);
        delete [] fatherName;
        delete [] name;
        return _ret;
    }
    else
        return name;
}

void 
DirectoryEntry::setName(char* name)
{
    nameSize = strlen(name);
    FileHeader* filenameHdr = new FileHeader();
    filenameHdr->FetchFrom(2); 
    OpenFile *filenameFile = new OpenFile(2);
    nameIndex = filenameHdr->FileLength();
    
    filenameFile->Seek(nameIndex);
    filenameFile->Write(name, nameSize);

    delete filenameHdr;
    delete filenameFile;

}


//----------------------------------------------------------------------
// Directory::Directory
// 	Initialize a directory; initially, the directory is completely
//	empty.  If the disk is being formatted, an empty directory
//	is all we need, but otherwise, we need to call FetchFrom in order
//	to initialize it from disk.
//
//	"size" is the number of entries in the directory
//----------------------------------------------------------------------

Directory::Directory(int size, int thisSector = 1, int fatherSector = 1)
{
    table = new DirectoryEntry[size];
    tableSize = size;

    table[meSector].inUse = TRUE;
    table[meSector].isDirectory = TRUE;
    table[meSector].sector = thisSector;

    table[paSector].inUse = TRUE;
    table[paSector].isDirectory = TRUE;
    table[paSector].sector = fatherSector;

    for (int i = 2; i < tableSize; i++)
    {
	   table[i].inUse = FALSE;
       table[i].isDirectory = FALSE;
   }
}

//----------------------------------------------------------------------
// Directory::~Directory
// 	De-allocate directory data structure.
//----------------------------------------------------------------------

Directory::~Directory()
{ 
    delete [] table;
} 

//----------------------------------------------------------------------
// Directory::FetchFrom
// 	Read the contents of the directory from disk.
//
//	"file" -- file containing the directory contents
//----------------------------------------------------------------------

void
Directory::FetchFrom(OpenFile *file)
{
    (void) file->ReadAt((char *)table, tableSize * sizeof(DirectoryEntry), 0);
}

//----------------------------------------------------------------------
// Directory::WriteBack
// 	Write any modifications to the directory back to disk
//
//	"file" -- file to contain the new directory contents
//----------------------------------------------------------------------

void
Directory::WriteBack(OpenFile *file)
{
    (void) file->WriteAt((char *)table, tableSize * sizeof(DirectoryEntry), 0);
}

//----------------------------------------------------------------------
// Directory::FindIndex
// 	Look up file name in directory, and return its location in the table of
//	directory entries.  Return -1 if the name isn't in the directory.
//
//	"name" -- the file name to look up
//----------------------------------------------------------------------

int
Directory::FindIndex(char *name)
{
    for (int i = 0; i < tableSize; i++)
        if (table[i].inUse && !strcmp(table[i].getName(), name))
	    return i;
    return -1;		// name not in directory
}

//----------------------------------------------------------------------
// Directory::Find
// 	Look up file name in directory, and return the disk sector number
//	where the file's header is stored. Return -1 if the name isn't 
//	in the directory.
//
//	"name" -- the file name to look up
//----------------------------------------------------------------------

int
Directory::Find(char *name, char *path = "/")
{
    if(strlen(path) == 1)
    {
        int i = FindIndex(name);

        if (i != -1)
           return table[i].sector;

       return -1;
    }
    else
    {
        char* end = path;
        end++;
        int length = 0;
        while(*end != '/')
        {
            end++;
        }
        char* begin = path;
        begin++;
        char pathName[length + 1];
        strncpy(pathName, begin, length);
        pathName[length] = '\0';

        int index = FindIndex(pathName);
        if (index == -1)
           return -1;

        if (table[index].isDirectory) 
        {
            int sector = table[index].sector;
            OpenFile* directoryFile = new OpenFile(sector);
            Directory* directory = new Directory(12, sector, 
                table[meSector].sector);
            directory->FetchFrom(directoryFile);
            int _ret = directory->Find(name, end);
            delete directoryFile;
            delete directory;
            return _ret;
        }
    }
    return -1;
}

//----------------------------------------------------------------------
// Directory::Add
// 	Add a file into the directory.  Return TRUE if successful;
//	return FALSE if the file name is already in the directory, or if
//	the directory is completely full, and has no more space for
//	additional file names.
//
//	"name" -- the name of the file being added
//	"newSector" -- the disk sector containing the added file's header
//----------------------------------------------------------------------

bool
Directory::Add(char *name, int newSector, bool isDirectory = FALSE, char* path = "/")
{ 
    if(strlen(path) == 1)
    {
        if (FindIndex(name) != -1)
    	   return FALSE;

        for (int i = 2; i < tableSize; i++)
        {
            if (!table[i].inUse) 
            {
                table[i].inUse = TRUE;
                table[i].isDirectory = isDirectory;
                table[i].setName(name); 
                table[i].sector = newSector;

 
                if(isDirectory)
                {
                    Directory* directory = new Directory(12, newSector,
                        table[meSector].sector);
                    OpenFile* directoryFile = new OpenFile(newSector);
                    directory->WriteBack(directoryFile);
                    delete directoryFile;
                    delete directory;
                }



                FileHeader* fileHdr = new FileHeader();
                fileHdr->FetchFrom(newSector);
                fileHdr->setFather(table[meSector].sector);
                fileHdr->setCreateTime();
                fileHdr->WriteBack(newSector);
                delete fileHdr;


                return TRUE;
            }
    	}
        return FALSE;	// no space.  Fix when we have extensible files.
    }
    else
    {
        char* end = path;
        end++;
        int length = 0;
        while(*end != '/')
        {
            end++;
            length++;
        }
        char* begin = path;
        begin++;
        char pathName[length + 1];
        strncpy(pathName, begin, length);
        pathName[length] = '\0';


        int index = FindIndex(pathName);
        if (index == -1)
           return -1;

        if (table[index].isDirectory) 
        {
            int sector = table[index].sector;
            OpenFile* directoryFile = new OpenFile(sector);
            Directory* directory = new Directory(12, sector, 
                table[meSector].sector);
            directory->FetchFrom(directoryFile);
            bool success = directory->Add(name, newSector,
                isDirectory, end);
            directory->WriteBack(directoryFile);
            delete directory;
            delete directoryFile;
            return success;
        }
    }
    return FALSE;
}

//----------------------------------------------------------------------
// Directory::Remove
// 	Remove a file name from the directory.  Return TRUE if successful;
//	return FALSE if the file isn't in the directory. 
//
//	"name" -- the file name to be removed
//----------------------------------------------------------------------

bool 
Directory::RemoveAll()
{
    for (int i = 2; i < tableSize; i++)
    {
        if(table[i].inUse)
        {
            if(table[i].isDirectory)
            {
                
                OpenFile* directoryFile = new OpenFile(table[i].sector);
                Directory* directory = new Directory(12);
                directory->FetchFrom(directoryFile);
                directory->RemoveAll();
                directory->WriteBack(directoryFile);
                delete directory;
                delete directoryFile;
            }
            //printf("removeFile in dir\n", table[i].name);
            table[i].inUse = FALSE;

            BitMap *freeMap;
            FileHeader *fileHdr;
            OpenFile *freeMapFile = new OpenFile(0);

            int sector = table[i].sector;

            fileHdr = new FileHeader;
            fileHdr->FetchFrom(sector);

            freeMap = new BitMap(NumSectors);
            freeMap->FetchFrom(freeMapFile);

            fileHdr->Deallocate(freeMap);       // remove data blocks
            freeMap->Clear(sector);         // remove header block

            freeMap->WriteBack(freeMapFile);        // flush to disk
            delete fileHdr;
            delete freeMap;
            delete freeMapFile;
        }
    }
}

bool
Directory::Remove(char *name, char* path = "/")
{ 
    if(strlen(path) == 1)
    {
        int i = FindIndex(name);
        if (i < 2)
            return FALSE;       // name not in directory
        if(table[i].isDirectory)
        {
            int sector = table[i].sector;
            OpenFile* directoryFile = new OpenFile(sector);
            Directory* directory = new Directory(12, sector, 
                table[meSector].sector);
            directory->FetchFrom(directoryFile);
            directory->RemoveAll();
            directory->WriteBack(directoryFile);
            delete directoryFile;
            delete directory;
        }
        table[i].inUse = FALSE;
        return TRUE;
    }
    else
    {
        char* end = path;
        end++;
        int length = 0;
        while(*end != '/')
        {
            end++;
            length++;
        }
        char* begin = path;
        begin++;
        char pathName[length + 1];
        strncpy(pathName, begin, length);
        pathName[length] = '\0';

        int index = FindIndex(pathName);
        if (index == -1)
           return -1;

        if (table[index].isDirectory) 
        {
            int sector = table[index].sector;
            OpenFile* directoryFile = new OpenFile(sector);
            Directory* directory = new Directory(12, sector, 
                table[meSector].sector);
            directory->FetchFrom(directoryFile);
            bool success = directory->Remove(name, end);
            directory->WriteBack(directoryFile);
            delete directory;
            delete directoryFile;
            return success;
        }
    }
    return FALSE;
}

//----------------------------------------------------------------------
// Directory::List
// 	List all the file names in the directory. 
//----------------------------------------------------------------------
void tab(int deep)
{
    for(int i = 0; i < deep; i++)
        printf("    ");
}

void
Directory::List(int deep = 0)
{
    tab(deep);
    printf("./\n");
    tab(deep);
    printf("../\n");
    for (int i = 2; i < tableSize; i++)
    {
    	if (table[i].inUse)
        {
            if(table[i].isDirectory)
            {
                tab(deep);
                printf("%s/\n", table[i].getName());
                OpenFile* directoryFile = new OpenFile(table[i].sector);
                Directory* directory = new Directory(12);
                directory->FetchFrom(directoryFile);
                directory->List(deep + 1);
                delete directory;
                delete directoryFile;
            }
            else
            {
                tab(deep);
    	        printf("%s\n", table[i].getName());
            }
        }
    }
}

//----------------------------------------------------------------------
// Directory::Print
// 	List all the file names in the directory, their FileHeader locations,
//	and the contents of each file.  For debugging.
//----------------------------------------------------------------------

char* 
Directory::getFileName(int sector)
{
    for (int i = 2; i < tableSize; i++)
    {
        if (table[i].inUse && table[i].sector == sector) 
        {
            return table[i].getName(table[meSector].sector);
        }
    }
    return NULL;
}

void
Directory::Print()
{ 
    FileHeader *hdr = new FileHeader;

    printf("Directory contents:\n");
    for (int i = 2; i < tableSize; i++)
	if (table[i].inUse) {
	    printf("Name: %s, Sector: %d\n", 
            table[i].getName(table[meSector].sector), table[i].sector);
	    hdr->FetchFrom(table[i].sector);
	    hdr->Print();
	}
    printf("\n");
    delete hdr;
}
