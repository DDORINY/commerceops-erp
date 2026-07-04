import type { User } from './types';

export const mockUsers: User[] = [
  {
    id: 1,
    email: 'admin@commerceops.com',
    name: '관리자',
    phone: '010-0000-0000',
    role: 'ADMIN',
    createdAt: '2026-01-01T00:00:00',
  },
  {
    id: 2,
    email: 'user@example.com',
    name: '김지은',
    phone: '010-1234-5678',
    role: 'USER',
    createdAt: '2026-03-15T10:00:00',
  },
  {
    id: 3,
    email: 'suyeon@example.com',
    name: '박수연',
    phone: '010-9876-5432',
    role: 'USER',
    createdAt: '2026-04-02T14:30:00',
  },
  {
    id: 4,
    email: 'hyejin@example.com',
    name: '이혜진',
    phone: '010-2222-3333',
    role: 'USER',
    createdAt: '2026-05-10T09:15:00',
  },
  {
    id: 5,
    email: 'minseo@example.com',
    name: '최민서',
    phone: '010-4444-5555',
    role: 'USER',
    createdAt: '2026-06-01T11:00:00',
  },
];
