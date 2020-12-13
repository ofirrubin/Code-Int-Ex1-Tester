from xlsxwriter import Workbook
import subprocess
import os


class ErrorKey:
    ProgramNotFound = ".java file not found"
    TooLong = "Too long runtime"
    Mistakes = "false"


class ExcelGrading:
    def __init__(self, file_name, running_command):
        self.workbook = Workbook(file_name + '.xlsx')
        self.worksheet = self.workbook.add_worksheet()
        self.running_command = running_command
        self.set_grades_titles()
        self.row = 1

    def set_grades_titles(self):
        title_style = self.workbook.add_format({'bold': 1, 'font_color': 'black'})

        self.worksheet.write('A1', 'Student ID', title_style)
        self.worksheet.write("B1", "Runtime", title_style)
        self.worksheet.write("C1", "Number Of Wrong Answers", title_style)
        self.worksheet.write("D1", "Number Of Delayed Answers", title_style)
        self.worksheet.write("E1", "Final Grade", title_style)

    def save_file(self):
        self.workbook.close()

    def add_grade(self, file_dir_path: str, student_id: str):
        process = subprocess.Popen(self.running_command + " " + file_dir_path,
                                   stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = process.communicate()
        try:
            stderr = stderr.decode()
            if ErrorKey.ProgramNotFound in stderr:
                grade = mistakes = rt = too_long = 0
            else:
                _, grade = stderr.split('Report: ', maxsplit=1)
                grade, _ = grade.split('\n************************************\r', maxsplit=1)

                grade, rt = grade[7:].replace(' ', '').split("runtime:")
                grade = float(grade)
                mistakes = stderr.count(ErrorKey.Mistakes)
                too_long = stderr.count(ErrorKey.TooLong)
        except (UnicodeDecodeError, ValueError, TypeError):
            grade = mistakes = rt = too_long = 0

        high_style = self.workbook.add_format({'bold': 1, 'font_color': 'green'})
        low_style = self.workbook.add_format({'bold': 1, 'font_color': 'red'})

        self.worksheet.write(self.row, 0, student_id)
        self.worksheet.write(self.row, 1, rt)
        self.worksheet.write(self.row, 2, mistakes)
        self.worksheet.write(self.row, 3, too_long)
        if 50 < grade < 90:
            self.worksheet.write_number(self.row, 4, grade)
        else:
            self.worksheet.write_number(self.row, 4, grade, high_style if grade > 90 else low_style)
        self.row += 1

    def set_chart(self):
        # Create a new chart object.
        chart = self.workbook.add_chart({'type': 'line'})
        # Add a series to the chart.
        chart.add_series({'values': '=Sheet1!$E$2:$E$' + str(self.row), 'trendline': {'type': 'linear'}})
        # Insert the chart into the worksheet.
        self.worksheet.insert_chart('G1', chart)


def main():
    run_command = "java -jar Ex1_Tester.jar"  # Command required to run the tester
    excel_file_path = "Grades"  # Excel file path with file name (no extension)
    parent_path = r"C:\Users\ofirr\Desktop"

    print("Auto Grading Program.\nStarting At: " + parent_path)
    grading = ExcelGrading(excel_file_path, run_command)
    grading.set_grades_titles()

    if os.path.exists(parent_path) is False:
        print("Folder not found.")
        exit(1)
    for dirs in os.listdir(parent_path):
        path = os.path.join(parent_path, dirs)
        if os.path.isdir(path) is True:
            grading.add_grade(path, dirs)
    print("All grades calculated. Creating graph...")
    grading.set_chart()
    print("Graph created. Saving file..")
    grading.save_file()
    print("All grades saved in Excel file. You may close the program.")


if __name__ == "__main__":
    main()
