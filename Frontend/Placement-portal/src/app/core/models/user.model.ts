export type Role = 'STUDENT' | 'FACULTY_MENTOR' | 'PLACEMENT_OFFICER' | 'EMPLOYER' | 'ADMIN';

export interface User {
  userId: string;
  email: string;
  fullName: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  role: Role;
  userId: string;
  fullName: string;
}
