export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  email?: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  username: string;
  role: string;
}

export interface UserProfile {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phone: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  email?: string;
  phone?: string;
}
