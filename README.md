# Ex1-Tester
Tester for Ex1 (Coding Introduction)

I fixed the tester of Ex1 so it work..
The code and script used to unzip and yet try open from the zip, which worked only if you use zip named Ex1 and without any folder in it.
My gets a path to parent folder and tests the Ex.1 no mather what name it's saved as.
In addition it accept with or without folder inside the ZIP, with or without zip (file might be just in the folder not zipped) etc.
The test takes the first .java file it finds by the following order:
If found in: ZIP -> In child dir -> In parent dir / Not found at all.

How to run Ex1_Tester (the java file):
Run the java file with the parent folder of .java / ZIP file. All the aruments are treated as one dir path (using space joinning).

How to export to Excel:
Note: You may want to change the script because it's very simple, done as an example and not for actual use.

Make a parent dir which includes dirs where their each of their name is the student ID and their .java / ZIP file are in them.
Set "parent_path" variable in the program as that path.

Set "run_command" variable as the run command to the tester which will be fed with each of the subfolders in "parent_path" as arguments.

Set "excel_file_path" variable as the path + file name (no extension) of the Excel file you want to export.

Run the script.
