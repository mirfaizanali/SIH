export interface ReportDto {
  id: string;
  enrollmentId: string;
  reportType: string;
  title: string;
  content: string;
  filePath: string;
  submittedAt: string;
  status: string;
  reviewerId: string;
  reviewerComments: string;
  reviewedAt: string;
}

export interface InternshipEnrollmentDto {
  id: string;
  studentId: string;
  studentName: string;
  internshipId: string;
  internshipTitle: string;
  facultyMentorId: string;
  mentorName: string;
  startDate: string;
  endDate: string;
  status: string;
}
