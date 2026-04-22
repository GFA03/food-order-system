import { http, HttpResponse } from 'msw';
import type {
  Restaurant,
  MenuItem,
  CuisineTag,
  Order,
  PaginatedResponse,
  UserWithProfile,
} from '../types';

// ── Sample data ──────────────────────────────────────────────────────────────

const TAGS: CuisineTag[] = [
  { id: 't1', name: 'Italian' },
  { id: 't2', name: 'Vegan' },
  { id: 't3', name: 'Sushi' },
  { id: 't4', name: 'Burgers' },
  { id: 't5', name: 'Mexican' },
  { id: 't6', name: 'Indian' },
  { id: 't7', name: 'Chinese' },
];

const RESTAURANTS: Restaurant[] = [
  { id: 'r1', name: 'Bella Italia', description: 'Authentic Italian cuisine', rating: 4.8, deliveryTime: 30, cuisineTags: [TAGS[0]] },
  { id: 'r2', name: 'Green Garden', description: 'Fresh vegan dishes', rating: 4.5, deliveryTime: 25, cuisineTags: [TAGS[1]] },
  { id: 'r3', name: 'Tokyo Bites', description: 'Premium sushi and ramen', rating: 4.7, deliveryTime: 40, cuisineTags: [TAGS[2]] },
  { id: 'r4', name: 'Burger Palace', description: 'Juicy gourmet burgers', rating: 4.3, deliveryTime: 20, cuisineTags: [TAGS[3]] },
  { id: 'r5', name: 'Taco Fiesta', description: 'Vibrant Mexican street food', rating: 4.6, deliveryTime: 35, cuisineTags: [TAGS[4]] },
  { id: 'r6', name: 'Spice Route', description: 'Rich Indian curries', rating: 4.4, deliveryTime: 45, cuisineTags: [TAGS[5]] },
  { id: 'r7', name: 'Dragon Wok', description: 'Classic Chinese takeaway', rating: 4.2, deliveryTime: 30, cuisineTags: [TAGS[6]] },
];

const MENU_ITEMS: Record<string, MenuItem[]> = {
  r1: [
    { id: 'm1', name: 'Margherita Pizza', description: 'Classic tomato and mozzarella', price: 12.99, restaurantId: 'r1' },
    { id: 'm2', name: 'Spaghetti Carbonara', description: 'Creamy pasta with pancetta', price: 14.99, restaurantId: 'r1' },
    { id: 'm3', name: 'Tiramisu', description: 'Classic Italian dessert', price: 6.99, restaurantId: 'r1' },
  ],
  r2: [
    { id: 'm4', name: 'Buddha Bowl', description: 'Quinoa, roasted veggies, tahini', price: 11.99, restaurantId: 'r2' },
    { id: 'm5', name: 'Avocado Toast', description: 'Sourdough with smashed avocado', price: 8.99, restaurantId: 'r2' },
  ],
  r3: [
    { id: 'm6', name: 'Salmon Nigiri (8pc)', description: 'Fresh Atlantic salmon', price: 16.99, restaurantId: 'r3' },
    { id: 'm7', name: 'Tonkotsu Ramen', description: 'Rich pork broth, soft egg', price: 13.99, restaurantId: 'r3' },
  ],
  r4: [
    { id: 'm8', name: 'Classic Cheeseburger', description: 'Beef patty, cheddar, pickles', price: 10.99, restaurantId: 'r4' },
    { id: 'm9', name: 'BBQ Bacon Burger', description: 'Smoky BBQ sauce, crispy bacon', price: 13.99, restaurantId: 'r4' },
    { id: 'm10', name: 'Fries', description: 'Crispy golden fries', price: 3.99, restaurantId: 'r4' },
  ],
  r5: [
    { id: 'm11', name: 'Beef Tacos (3pc)', description: 'Seasoned beef, salsa, guac', price: 9.99, restaurantId: 'r5' },
    { id: 'm12', name: 'Chicken Burrito', description: 'Grilled chicken, rice, beans', price: 11.99, restaurantId: 'r5' },
  ],
  r6: [
    { id: 'm13', name: 'Butter Chicken', description: 'Creamy tomato curry', price: 13.99, restaurantId: 'r6' },
    { id: 'm14', name: 'Garlic Naan', description: 'Freshly baked flatbread', price: 2.99, restaurantId: 'r6' },
  ],
  r7: [
    { id: 'm15', name: 'Kung Pao Chicken', description: 'Spicy stir-fry with peanuts', price: 12.99, restaurantId: 'r7' },
    { id: 'm16', name: 'Spring Rolls (4pc)', description: 'Crispy vegetable rolls', price: 5.99, restaurantId: 'r7' },
  ],
};

const SAMPLE_ORDERS: Order[] = [
  {
    id: 'o1',
    status: 'DELIVERED',
    createdAt: '2026-04-10T12:00:00Z',
    total: 27.98,
    restaurantName: 'Bella Italia',
    items: [
      { id: 'oi1', menuItemName: 'Margherita Pizza', quantity: 1, price: 12.99 },
      { id: 'oi2', menuItemName: 'Tiramisu', quantity: 2, price: 6.99 },
    ],
  },
  {
    id: 'o2',
    status: 'CONFIRMED',
    createdAt: '2026-04-18T18:30:00Z',
    total: 13.99,
    restaurantName: 'Tokyo Bites',
    items: [
      { id: 'oi3', menuItemName: 'Tonkotsu Ramen', quantity: 1, price: 13.99 },
    ],
  },
];

type MockAuthUser = {
  id: string;
  email: string;
  name: string;
  password: string;
  roles: string[];
  profile: UserWithProfile['profile'];
};

const SEEDED_PROFILE: UserWithProfile['profile'] = {
  deliveryAddress: 'Strada Academiei 14, Bucuresti',
  latitude: 44.4362,
  longitude: 26.0976,
  dietaryPreferences: ['Vegan'],
};

const SEEDED_USER: MockAuthUser = {
  id: 'u1',
  email: 'rares.papusoi@omnieats.dev',
  name: 'Rares Papusoi',
  password: 'password123',
  roles: ['USER', 'ADMIN'],
  profile: SEEDED_PROFILE,
};

let nextMockUserId = 2;

const mockUsersByEmail = new Map<string, MockAuthUser>([
  [SEEDED_USER.email.toLowerCase(), { ...SEEDED_USER, profile: { ...SEEDED_PROFILE } }],
]);

const mockUsersById = new Map<string, MockAuthUser>([
  [SEEDED_USER.id, { ...SEEDED_USER, profile: { ...SEEDED_PROFILE } }],
]);

// ── Helper ────────────────────────────────────────────────────────────────────

function paginate<T>(items: T[], page: number, size: number): PaginatedResponse<T> {
  const start = page * size;
  const content = items.slice(start, start + size);
  return {
    content,
    totalPages: Math.ceil(items.length / size),
    totalElements: items.length,
    size,
    number: page,
  };
}

function toUserWithProfile(user: MockAuthUser): UserWithProfile {
  return {
    id: user.id,
    email: user.email,
    name: user.name,
    roles: [...user.roles],
    profile: { ...user.profile },
  };
}

function toAuthUser(user: MockAuthUser) {
  return {
    id: user.id,
    email: user.email,
    name: user.name,
    roles: [...user.roles],
  };
}

function issueMockToken(user: MockAuthUser): string {
  const payload = btoa(JSON.stringify({
    sub: user.id,
    email: user.email,
    name: user.name,
    roles: user.roles,
    exp: Date.now() / 1000 + 3600,
  }));
  return `eyJhbGciOiJIUzI1NiJ9.${payload}.fake-signature`;
}

function parseMockToken(token: string): { sub?: string } {
  try {
    const payload = token.split('.')[1];
    if (!payload) return {};
    return JSON.parse(atob(payload)) as { sub?: string };
  } catch {
    return {};
  }
}

function getUserFromRequest(request: Request): MockAuthUser | null {
  const authHeader = request.headers.get('Authorization');
  if (!authHeader?.startsWith('Bearer ')) return null;
  const token = authHeader.slice('Bearer '.length);
  const payload = parseMockToken(token);
  if (!payload.sub) return null;
  return mockUsersById.get(payload.sub) ?? null;
}

// ── Handlers ──────────────────────────────────────────────────────────────────

export const handlers = [
  // Auth
  http.post('/api/auth/login', async ({ request }) => {
    const body = await request.json() as { email: string; password: string };
    const email = body.email?.trim().toLowerCase();
    const password = body.password?.trim();
    if (!email || !password) {
      return HttpResponse.json({ message: 'Email and password are required.' }, { status: 400 });
    }

    const user = mockUsersByEmail.get(email);
    if (!user || user.password !== password) {
      return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
    }

    return HttpResponse.json({
      token: issueMockToken(user),
      user: toAuthUser(user),
    });
  }),

  http.post('/api/auth/register', async ({ request }) => {
    const body = await request.json() as { name: string; email: string; password: string };
    const name = body.name?.trim();
    const email = body.email?.trim().toLowerCase();
    const password = body.password?.trim();

    if (!name || !email || !password) {
      return HttpResponse.json({ message: 'Name, email, and password are required.' }, { status: 400 });
    }

    if (mockUsersByEmail.has(email)) {
      return HttpResponse.json({ message: 'This email is already in use.' }, { status: 409 });
    }

    const newUser: MockAuthUser = {
      id: `u${nextMockUserId++}`,
      name,
      email,
      password,
      roles: ['USER'],
      profile: { ...SEEDED_PROFILE },
    };

    mockUsersByEmail.set(email, newUser);
    mockUsersById.set(newUser.id, newUser);

    return HttpResponse.json({ id: newUser.id, email: newUser.email, name: newUser.name }, { status: 201 });
  }),

  // Users
  http.get('/api/users/me', ({ request }) => {
    const user = getUserFromRequest(request);
    if (!user) return HttpResponse.json({ message: 'Unauthorized' }, { status: 401 });
    return HttpResponse.json(toUserWithProfile(user));
  }),

  http.put('/api/users/me', async ({ request }) => {
    const user = getUserFromRequest(request);
    if (!user) return HttpResponse.json({ message: 'Unauthorized' }, { status: 401 });

    const body = await request.json() as Partial<UserWithProfile>;
    if (typeof body.name === 'string' && body.name.trim()) {
      user.name = body.name.trim();
    }
    if (body.profile) {
      user.profile = { ...user.profile, ...body.profile };
    }

    return HttpResponse.json(toUserWithProfile(user));
  }),

  // Restaurants
  http.get('/api/restaurants', ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') ?? '0');
    const size = parseInt(url.searchParams.get('size') ?? '10');
    const sort = url.searchParams.get('sort') ?? 'rating,desc';
    const tags = url.searchParams.getAll('tags');

    let results = [...RESTAURANTS];

    if (tags.length > 0) {
      results = results.filter((r) =>
        r.cuisineTags.some((t) => tags.includes(t.name))
      );
    }

    if (sort.startsWith('rating')) {
      results.sort((a, b) => sort.includes('asc') ? a.rating - b.rating : b.rating - a.rating);
    } else if (sort.startsWith('deliveryTime')) {
      results.sort((a, b) => sort.includes('desc') ? b.deliveryTime - a.deliveryTime : a.deliveryTime - b.deliveryTime);
    }

    return HttpResponse.json(paginate(results, page, size));
  }),

  http.get('/api/restaurants/tags', () => HttpResponse.json(TAGS)),

  http.get('/api/restaurants/:id', ({ params }) => {
    const restaurant = RESTAURANTS.find((r) => r.id === params.id);
    if (!restaurant) return HttpResponse.json({ message: 'Not found' }, { status: 404 });
    return HttpResponse.json(restaurant);
  }),

  http.get('/api/restaurants/:id/menu', ({ params, request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') ?? '0');
    const size = parseInt(url.searchParams.get('size') ?? '10');
    const sort = url.searchParams.get('sort') ?? 'price,asc';
    const items = MENU_ITEMS[params.id as string] ?? [];
    const sorted = [...items].sort((a, b) =>
      sort.includes('desc') ? b.price - a.price : a.price - b.price
    );
    return HttpResponse.json(paginate(sorted, page, size));
  }),

  // Admin — Restaurants
  http.post('/api/restaurants', async ({ request }) => {
    const body = await request.json() as Partial<Restaurant>;
    return HttpResponse.json({ id: `r${Date.now()}`, cuisineTags: [], ...body }, { status: 201 });
  }),

  http.put('/api/restaurants/:id', async ({ params, request }) => {
    const body = await request.json() as Partial<Restaurant>;
    const existing = RESTAURANTS.find((r) => r.id === params.id);
    return HttpResponse.json({ ...existing, ...body });
  }),

  http.delete('/api/restaurants/:id', () => new HttpResponse(null, { status: 204 })),

  // Admin — Menu items
  http.post('/api/restaurants/:id/menu', async ({ params, request }) => {
    const body = await request.json() as Partial<MenuItem>;
    return HttpResponse.json({ id: `m${Date.now()}`, restaurantId: params.id, ...body }, { status: 201 });
  }),

  http.put('/api/restaurants/:restaurantId/menu/:itemId', async ({ request }) => {
    const body = await request.json() as Partial<MenuItem>;
    return HttpResponse.json(body);
  }),

  http.delete('/api/restaurants/:restaurantId/menu/:itemId', () => new HttpResponse(null, { status: 204 })),

  // Admin — Tags
  http.post('/api/restaurants/tags', async ({ request }) => {
    const body = await request.json() as { name: string };
    return HttpResponse.json({ id: `t${Date.now()}`, name: body.name }, { status: 201 });
  }),

  http.delete('/api/restaurants/tags/:id', () => new HttpResponse(null, { status: 204 })),

  // Orders
  http.post('/api/orders', async ({ request }) => {
    const body = await request.json() as { restaurantId: string; items: { menuItemId: string; quantity: number }[] };
    const restaurant = RESTAURANTS.find((r) => r.id === body.restaurantId);
    return HttpResponse.json({
      id: `o${Date.now()}`,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
      total: 25.00,
      restaurantName: restaurant?.name ?? 'Unknown',
      items: body.items.map((i, idx) => ({
        id: `oi${idx}`,
        menuItemName: 'Item',
        quantity: i.quantity,
        price: 12.50,
      })),
    }, { status: 201 });
  }),

  http.get('/api/orders', ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') ?? '0');
    const size = parseInt(url.searchParams.get('size') ?? '10');
    return HttpResponse.json(paginate(SAMPLE_ORDERS, page, size));
  }),

  http.get('/api/orders/:id', ({ params }) => {
    const order = SAMPLE_ORDERS.find((o) => o.id === params.id);
    if (!order) return HttpResponse.json({ message: 'Not found' }, { status: 404 });
    return HttpResponse.json(order);
  }),

  // AI
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
