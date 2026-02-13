export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phone: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

export type UserRole = 'ADMIN' | 'MANAGER' | 'WAITER' | 'KITCHEN' | 'CUSTOMER';
