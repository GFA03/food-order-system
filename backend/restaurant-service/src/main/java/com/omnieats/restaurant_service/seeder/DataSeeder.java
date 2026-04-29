package com.omnieats.restaurant_service.seeder;

import com.omnieats.restaurant_service.model.CuisineTag;
import com.omnieats.restaurant_service.model.MenuItem;
import com.omnieats.restaurant_service.model.Restaurant;
import com.omnieats.restaurant_service.repository.CuisineTagRepository;
import com.omnieats.restaurant_service.repository.MenuItemRepository;
import com.omnieats.restaurant_service.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final CuisineTagRepository cuisineTagRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public DataSeeder(CuisineTagRepository cuisineTagRepository,
                      RestaurantRepository restaurantRepository,
                      MenuItemRepository menuItemRepository) {
        this.cuisineTagRepository = cuisineTagRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (cuisineTagRepository.count() == 0) {
            logger.info("Seeding initial data...");

            // 1. Create Cuisine Tags
            CuisineTag italian = cuisineTagRepository.save(new CuisineTag("Italian"));
            CuisineTag vegan = cuisineTagRepository.save(new CuisineTag("Vegan"));
            CuisineTag fastFood = cuisineTagRepository.save(new CuisineTag("Fast Food"));
            CuisineTag asian = cuisineTagRepository.save(new CuisineTag("Asian"));

            // 2. Create Restaurants
            Restaurant rest1 = new Restaurant(
                    "Luigi's Trattoria",
                    "Authentic Italian pasta and pizza.",
                    4.8,
                    30,
                    List.of(italian)
            );
            rest1 = restaurantRepository.save(rest1);

            Restaurant rest2 = new Restaurant(
                    "Green Leaf Bowl",
                    "Healthy vegan bowls and smoothies.",
                    4.5,
                    20,
                    List.of(vegan)
            );
            rest2 = restaurantRepository.save(rest2);

            Restaurant rest3 = new Restaurant(
                    "Speedy Burger",
                    "Fast, delicious, and affordable burgers.",
                    4.2,
                    15,
                    List.of(fastFood)
            );
            rest3 = restaurantRepository.save(rest3);

            Restaurant rest4 = new Restaurant(
                    "Sakura Sushi",
                    "Premium sushi and Japanese delicacies.",
                    4.9,
                    40,
                    List.of(asian)
            );
            rest4 = restaurantRepository.save(rest4);

            // 3. Create Menu Items
            menuItemRepository.save(new MenuItem("Margherita Pizza", "Classic tomato and mozzarella.", new BigDecimal("12.99"), rest1));
            menuItemRepository.save(new MenuItem("Spaghetti Carbonara", "Creamy pasta with pancetta.", new BigDecimal("14.50"), rest1));

            menuItemRepository.save(new MenuItem("Buddha Bowl", "Quinoa, roasted veggies, tahini dressing.", new BigDecimal("11.00"), rest2));
            menuItemRepository.save(new MenuItem("Green Detox Smoothie", "Spinach, kale, apple, and ginger.", new BigDecimal("6.50"), rest2));

            menuItemRepository.save(new MenuItem("Cheeseburger", "Beef patty, cheddar, lettuce, tomato.", new BigDecimal("8.99"), rest3));
            menuItemRepository.save(new MenuItem("French Fries", "Crispy golden fries.", new BigDecimal("3.50"), rest3));

            menuItemRepository.save(new MenuItem("Spicy Tuna Roll", "Fresh tuna with spicy mayo.", new BigDecimal("9.00"), rest4));
            menuItemRepository.save(new MenuItem("Salmon Nigiri", "Fresh salmon over pressed rice.", new BigDecimal("12.00"), rest4));

            logger.info("Database seeding completed.");
        } else {
            logger.info("Database already contains data, skipping seeding.");
        }
    }
}
