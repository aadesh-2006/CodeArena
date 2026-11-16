import { request } from './client';
import {
  ApiResponse,
  PageResponse,
  ProblemQueryFilters,
  ProblemSummary,
  PublicProblemDetail,
  TagSummary,
  TopicSummary,
} from '../types';

export const problemApi = {
  getProblems: async (
    filters: ProblemQueryFilters = {}
  ): Promise<ApiResponse<PageResponse<ProblemSummary>>> => {
    const params = new URLSearchParams();

    if (filters.page !== undefined) {
      params.set('page', filters.page.toString());
    }
    if (filters.size !== undefined) {
      params.set('size', filters.size.toString());
    }
    if (filters.search && filters.search.trim()) {
      params.set('search', filters.search.trim());
    }
    if (filters.difficulty) {
      params.set('difficulty', filters.difficulty);
    }
    if (filters.topic && filters.topic.trim()) {
      params.set('topic', filters.topic.trim());
    }
    if (filters.tag && filters.tag.trim()) {
      params.set('tag', filters.tag.trim());
    }
    if (filters.sortBy) {
      params.set('sortBy', filters.sortBy);
    }
    if (filters.sortDirection) {
      params.set('sortDirection', filters.sortDirection);
    }

    const queryString = params.toString() ? `?${params.toString()}` : '';
    return request<PageResponse<ProblemSummary>>(`/api/problems${queryString}`, {
      method: 'GET',
    });
  },

  getProblemBySlug: async (slug: string): Promise<ApiResponse<PublicProblemDetail>> => {
    return request<PublicProblemDetail>(`/api/problems/${encodeURIComponent(slug)}`, {
      method: 'GET',
    });
  },

  getTopics: async (): Promise<ApiResponse<TopicSummary[]>> => {
    return request<TopicSummary[]>('/api/topics', {
      method: 'GET',
    });
  },

  getTags: async (): Promise<ApiResponse<TagSummary[]>> => {
    return request<TagSummary[]>('/api/tags', {
      method: 'GET',
    });
  },
};
