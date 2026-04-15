import { SkillDto } from './job.model';

export interface StudentProfileDto {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  rollNumber: string;
  department: string;
  batchYear: number;
  cgpa: number;
  phone: string;
  linkedinUrl: string;
  githubUrl: string;
  bio: string;
  isPlaced: boolean;
  placementPackage: number;
  placedCompany: string;
  facultyMentorId: string;
  preferredLocations?: string;
  preferredJobTypes?: string;
  skills: SkillDto[];
  createdAt: string;
}

export interface ResumeDto {
  id: string;
  studentProfileId: string;
  fileName: string;
  fileSizeBytes: number;
  contentType: string;
  isPrimary: boolean;
  uploadedAt: string;
}
