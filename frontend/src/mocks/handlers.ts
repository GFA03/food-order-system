import { http, HttpResponse } from 'msw';

// ── Handlers ──────────────────────────────────────────────────────────────────
// Only /api/ai/* is mocked — all other endpoints are served by the real backend.
// See frontend/TODO-backend-gaps.md for the implementation status of this feature.

export const handlers = [
  http.post('/api/ai/prompt', async ({ request }) => {
    const body = await request.json() as { message: string };
    const msg = body.message?.toLowerCase() ?? '';
    const suggestions = msg.includes('vegan')
      ? [{ type: 'restaurant', id: 'r2', name: 'Green Garden', description: 'Fresh vegan dishes', reason: 'Fully vegan menu with fresh ingredients' }]
      : [
          { type: 'restaurant', id: 'r1', name: 'Bella Italia', description: 'Authentic Italian cuisine', reason: 'Highly rated Italian restaurant near you' },
          { type: 'menuItem', id: 'm7', name: 'Tonkotsu Ramen', description: 'Rich pork broth, soft egg', reason: 'Popular comfort food choice' },
        ];
    return HttpResponse.json({ suggestions });
  }),
];
