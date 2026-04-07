export interface SkillDto {
  id: number;
  name: string;
  category: string;
  proficiencyLevel?: string;
}

export interface JobDto {
  id: string;
  employerId: string;
  companyName: string;
  title: string;
  description: string;
  location: string;
  jobType: string;
  experienceLevel: string;
  minCgpa: number;
  salaryMin: number;
  salaryMax: number;
  openingsCount: number;
  applicationDeadline: string;
  status: string;
  skills: SkillDto[];
  createdAt: string;
  matchScore?: number;
}

export interface InternshipDto {
  id: string;
  employerId: string;
  companyName: string;
  title: string;
  description: string;
  durationMonths: number;
  stipend: number;
  location: string;
  isRemote: boolean;
  minCgpa: number;
  applicationDeadline: string;
  status: string;
  createdAt: string;
}
