import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAiPrompt } from '../api/ai';
import type { AiSuggestion } from '../types';

function SuggestionCard({ suggestion }: { suggestion: AiSuggestion }) {
  const href = suggestion.type === 'restaurant'
    ? `/restaurants/${suggestion.id}`
    : `/restaurants/${suggestion.id}`;

  return (
    <Link to={href} className="block bg-white rounded-xl border border-gray-100 p-5 hover:shadow-md hover:border-orange-200 transition-all">
      <div className="flex items-start gap-3">
        <span className="text-2xl" aria-hidden="true">{suggestion.type === 'restaurant' ? '🍽️' : '🍴'}</span>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <h3 className="font-semibold text-gray-800">{suggestion.name}</h3>
            <span className="px-2 py-0.5 bg-orange-50 text-orange-600 text-xs font-medium rounded-full capitalize">
              {suggestion.type === 'restaurant' ? 'Restaurant' : 'Menu Item'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-0.5">{suggestion.description}</p>
          <p className="text-sm text-orange-600 mt-2 italic">"{suggestion.reason}"</p>
        </div>
      </div>
    </Link>
  );
}

export default function AiSearchPage() {
  const [prompt, setPrompt] = useState('');
  const aiPrompt = useAiPrompt();

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!prompt.trim()) return;
    aiPrompt.mutate({ message: prompt.trim() });
  }

  const isRateLimit = aiPrompt.isError && (aiPrompt.error as { response?: { status?: number } })?.response?.status === 429;
  const isServerError = aiPrompt.isError && !isRateLimit;

  return (
    <div className="max-w-2xl mx-auto">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-800">AI Food Search</h1>
        <p className="text-gray-500 mt-2">Describe what you're craving and let AI find it for you</p>
      </div>

      <form onSubmit={handleSubmit} className="flex gap-3 mb-8">
        <input
          type="text"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="I want something vegan and spicy…"
          className="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-transparent"
          aria-label="Food preference prompt"
          disabled={aiPrompt.isPending}
        />
        <button
          type="submit"
          disabled={aiPrompt.isPending || !prompt.trim()}
          className="px-6 py-3 bg-orange-500 text-white font-semibold rounded-xl hover:bg-orange-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {aiPrompt.isPending ? (
            <span className="flex items-center gap-2">
              <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" aria-hidden="true" />
              Thinking…
            </span>
          ) : 'Ask AI'}
        </button>
      </form>

      {isRateLimit && (
        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-xl mb-6">
          <p className="text-yellow-800 font-medium">Too many requests</p>
          <p className="text-yellow-700 text-sm mt-1">You've hit the AI rate limit. Please wait a moment before trying again.</p>
        </div>
      )}

      {isServerError && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl mb-6">
          <p className="text-red-800 font-medium">AI service unavailable</p>
          <p className="text-red-700 text-sm mt-1">The AI service is temporarily unavailable. Please try again later or browse restaurants manually.</p>
        </div>
      )}

      {aiPrompt.data && aiPrompt.data.suggestions.length > 0 && (
        <div>
          <h2 className="text-lg font-semibold text-gray-700 mb-4">
            {aiPrompt.data.suggestions.length} suggestion{aiPrompt.data.suggestions.length !== 1 ? 's' : ''} for you
          </h2>
          <div className="space-y-3">
            {aiPrompt.data.suggestions.map((s) => <SuggestionCard key={`${s.type}-${s.id}`} suggestion={s} />)}
          </div>
        </div>
      )}

      {aiPrompt.data && aiPrompt.data.suggestions.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <p>No suggestions found. Try a different prompt!</p>
        </div>
      )}

      {!aiPrompt.data && !aiPrompt.isPending && !aiPrompt.isError && (
        <div className="text-center py-12 text-gray-400">
          <p className="text-5xl mb-4">🤖</p>
          <p>Ask me anything about food and I'll find the best options for you.</p>
          <div className="flex flex-wrap justify-center gap-2 mt-4">
            {['Vegan options', 'Quick delivery', 'Best rated', 'Spicy food'].map((example) => (
              <button key={example} onClick={() => setPrompt(example)} className="px-3 py-1.5 text-sm border border-gray-200 rounded-full hover:border-orange-300 hover:text-orange-500 transition-colors">
                {example}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
