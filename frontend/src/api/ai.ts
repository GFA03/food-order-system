import { useMutation } from '@tanstack/react-query';
import apiClient from './client';
import type { AiSuggestion } from '../types';

interface AiPromptRequest {
  message: string;
}

interface AiPromptResponse {
  suggestions: AiSuggestion[];
}

export function useAiPrompt() {
  return useMutation({
    mutationFn: (data: AiPromptRequest) =>
      apiClient.post<AiPromptResponse>('/api/ai/prompt', data).then((r) => r.data),
  });
}
