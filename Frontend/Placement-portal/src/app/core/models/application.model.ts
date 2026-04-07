export interface ApplicationDto {
  id: string;
  studentId: string;
  studentName: string;
  jobId: string;
  jobTitle: string;
  internshipId: string;
  internshipTitle: string;
  resumeId: string;
  coverLetter: string;
  status: string;
  appliedAt: string;
}

export interface InterviewDto {
  id: string;
  applicationId: string;
  roundNumber: number;
  interviewType: string;
  scheduledAt: string;
  durationMins: number;
  meetingLink: string;
  location: string;
  status: string;
  feedback: string;
  score: number;
}
