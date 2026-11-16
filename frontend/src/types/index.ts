export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface HealthStatus {
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  service: string;
  version: string;
  timestamp: string;
}

export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type ProblemType = 'ALGORITHMIC' | 'DATA_STRUCTURE' | 'SYSTEM_DESIGN';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface TopicSummary {
  id: number;
  name: string;
  slug: string;
  description?: string | null;
}

export interface TagSummary {
  id: number;
  name: string;
  slug: string;
}

export interface ProblemSummary {
  id: number;
  slug: string;
  title: string;
  difficulty: Difficulty;
  problemType: ProblemType;
  topics: TopicSummary[];
  tags: TagSummary[];
  createdAt: string;
}

export interface ProblemExample {
  id?: number | null;
  inputText: string;
  outputText: string;
  explanation?: string | null;
  orderIndex: number;
}

export interface ProblemHint {
  id?: number | null;
  hintText: string;
  orderIndex: number;
}

export interface PublicProblemDetail {
  slug: string;
  title: string;
  statement: string;
  difficulty: Difficulty;
  problemType: ProblemType;
  constraintsText: string;
  inputFormat: string;
  outputFormat: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  editorial?: string | null;
  topics: TopicSummary[];
  tags: TagSummary[];
  examples: ProblemExample[];
  hints: ProblemHint[];
  createdAt: string;
  updatedAt: string;
}

export interface ProblemQueryFilters {
  page?: number;
  size?: number;
  search?: string;
  difficulty?: Difficulty;
  topic?: string;
  tag?: string;
  sortBy?: 'title' | 'difficulty' | 'createdAt';
  sortDirection?: 'ASC' | 'DESC';
}

export interface Topic {
  id: number;
  name: string;
  slug: string;
}

export type UserRole = 'USER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  email: string;
  displayName?: string | null;
  role: UserRole;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  expiresInMs: number;
  user: User;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface UpdateProfileRequest {
  username?: string;
  displayName?: string;
}
