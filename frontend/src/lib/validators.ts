import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('Please enter a valid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  rememberMe: z.boolean(),
});

export const registerSchema = z
  .object({
    name: z.string().min(2, 'Name must be at least 2 characters'),
    email: z.string().email('Please enter a valid email'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

export const profileSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  deliveryAddress: z.string().min(5, 'Please enter a valid address'),
  latitude: z.number().optional(),
  longitude: z.number().optional(),
  dietaryPreferences: z.array(z.string()).optional(),
});

export const restaurantSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  description: z.string().min(5, 'Description is required'),
  rating: z.number().min(0).max(5),
  deliveryTime: z.number().min(1, 'Delivery time must be at least 1 minute'),
  cuisineTagIds: z.array(z.string()).optional(),
});

export const menuItemSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  description: z.string().min(5, 'Description is required'),
  price: z.number().min(0.01, 'Price must be greater than 0'),
});

export const cuisineTagSchema = z.object({
  name: z.string().min(2, 'Tag name is required'),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type ProfileFormData = z.infer<typeof profileSchema>;
export type RestaurantFormData = z.infer<typeof restaurantSchema>;
export type MenuItemFormData = z.infer<typeof menuItemSchema>;
export type CuisineTagFormData = z.infer<typeof cuisineTagSchema>;
