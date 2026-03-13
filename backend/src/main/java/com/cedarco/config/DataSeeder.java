package com.cedarco.config;

import com.cedarco.entity.*;
import com.cedarco.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepo,
            CategoryRepository categoryRepo,
            CollectionRepository collectionRepo,
            ProductRepository productRepo,
            CouponRepository couponRepo,
            BannerRepository bannerRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (productRepo.count() > 0) {
                log.info("Data already seeded, skipping...");
                return;
            }

            log.info("Seeding Cedar & Co. database...");

            // ─── Users ─────────────────────────────────────────────────
            if (userRepo.count() == 0) {
                User admin = User.builder()
                        .firstName("Admin").lastName("Cedar")
                        .email("admin@cedarco.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(User.Role.ADMIN).build();
                User customer = User.builder()
                        .firstName("Layla").lastName("Khoury")
                        .email("testverify@example.com")
                        .password(passwordEncoder.encode("Test@123"))
                        .role(User.Role.CUSTOMER).build();
                userRepo.saveAll(List.of(admin, customer));
            }

            // ─── Categories ────────────────────────────────────────────
            Category tops = Category.builder().name("Tops").slug("tops").description("Shirts, blouses, and tees").build();
            Category bottoms = Category.builder().name("Bottoms").slug("bottoms").description("Trousers, skirts, and shorts").build();
            Category outerwear = Category.builder().name("Outerwear").slug("outerwear").description("Coats, jackets, and kaftans").build();
            Category dresses = Category.builder().name("Dresses").slug("dresses").description("Maxi dresses, midi dresses, and sets").build();
            Category accessories = Category.builder().name("Accessories").slug("accessories").description("Bags, scarves, and more").build();
            categoryRepo.saveAll(List.of(tops, bottoms, outerwear, dresses, accessories));

            // ─── Collections ──────────────────────────────────────────
            Collection hamraNights = Collection.builder().name("Hamra Nights").slug("hamra-nights")
                    .description("Beirut street style — urban cool and nightlife vibes").season("SS25").build();
            Collection cedarsSnow = Collection.builder().name("Cedars & Snow").slug("cedars-snow")
                    .description("Mountain warmth in earth tones and winter wool").season("AW24").build();
            Collection medSummer = Collection.builder().name("Mediterranean Summer").slug("mediterranean-summer")
                    .description("Coastal living, linen lightness, and warm sea tones").season("SS25").build();
            Collection soukHeritage = Collection.builder().name("Souk Heritage").slug("souk-heritage")
                    .description("Traditional embroidery and artisan craftsmanship").season("AW24").build();
            Collection festiveBeirut = Collection.builder().name("Festive Beirut").slug("festive-beirut")
                    .description("Celebration, Eid, weddings — pure elegance").season("SS25").build();
            collectionRepo.saveAll(List.of(hamraNights, cedarsSnow, medSummer, soukHeritage, festiveBeirut));

            // ─── Products ─────────────────────────────────────────────
            List<Product> products = new ArrayList<>();

            // Helper to build product with sub-entities
            Product hamraShirt = Product.builder()
                    .name("Hamra Dusk Linen Shirt").slug("hamra-dusk-linen-shirt")
                    .description("A relaxed linen shirt in dusty terracotta, perfect for warm Beirut evenings.")
                    .price(new BigDecimal("89.00")).sku("HDL-001")
                    .category(tops).collection(medSummer).gender(Product.Gender.MEN)
                    .featured(true).newArrival(true).bestSeller(false).rating(4.5).reviewCount(12).build();
            
            hamraShirt.setImages(List.of(ProductImage.builder().url("https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600").product(hamraShirt).build()));
            hamraShirt.setColors(List.of(
                ProductColor.builder().name("Terracotta").product(hamraShirt).build(),
                ProductColor.builder().name("Ivory").product(hamraShirt).build()
            ));
            hamraShirt.setSizes(List.of(
                ProductSize.builder().name("S").product(hamraShirt).build(),
                ProductSize.builder().name("M").product(hamraShirt).build(),
                ProductSize.builder().name("L").product(hamraShirt).build(),
                ProductSize.builder().name("XL").product(hamraShirt).build()
            ));
            hamraShirt.setStock(List.of(
                ProductStock.builder().size("S").quantity(10).product(hamraShirt).build(),
                ProductStock.builder().size("M").quantity(15).product(hamraShirt).build(),
                ProductStock.builder().size("L").quantity(12).product(hamraShirt).build(),
                ProductStock.builder().size("XL").quantity(8).product(hamraShirt).build()
            ));
            products.add(hamraShirt);

            Product byblosBlouse = Product.builder()
                    .name("Byblos Embroidered Blouse").slug("byblos-embroidered-blouse")
                    .description("Handcrafted embroidery inspired by ancient Byblos — a wearable heritage piece.")
                    .price(new BigDecimal("145.00")).sku("BEB-001")
                    .category(tops).collection(soukHeritage).gender(Product.Gender.WOMEN)
                    .featured(true).newArrival(false).bestSeller(true).rating(4.8).reviewCount(24).build();
            
            byblosBlouse.setImages(List.of(ProductImage.builder().url("https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600").product(byblosBlouse).build()));
            byblosBlouse.setColors(List.of(
                ProductColor.builder().name("Ivory").product(byblosBlouse).build(),
                ProductColor.builder().name("Sage").product(byblosBlouse).build()
            ));
            byblosBlouse.setSizes(List.of(
                ProductSize.builder().name("XS").product(byblosBlouse).build(),
                ProductSize.builder().name("S").product(byblosBlouse).build(),
                ProductSize.builder().name("M").product(byblosBlouse).build(),
                ProductSize.builder().name("L").product(byblosBlouse).build()
            ));
            byblosBlouse.setStock(List.of(
                ProductStock.builder().size("XS").quantity(5).product(byblosBlouse).build(),
                ProductStock.builder().size("S").quantity(8).product(byblosBlouse).build(),
                ProductStock.builder().size("M").quantity(10).product(byblosBlouse).build(),
                ProductStock.builder().size("L").quantity(6).product(byblosBlouse).build()
            ));
            products.add(byblosBlouse);

            Product cedarCoat = Product.builder()
                    .name("Cedar Wool Overcoat").slug("cedar-wool-overcoat")
                    .description("A structured wool overcoat in midnight blue — built for mountain winters.")
                    .price(new BigDecimal("320.00")).sku("CWO-001")
                    .category(outerwear).collection(cedarsSnow).gender(Product.Gender.UNISEX)
                    .featured(true).newArrival(false).bestSeller(true).rating(4.9).reviewCount(31).build();
            
            cedarCoat.setImages(List.of(ProductImage.builder().url("https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600").product(cedarCoat).build()));
            cedarCoat.setColors(List.of(
                ProductColor.builder().name("Midnight").product(cedarCoat).build(),
                ProductColor.builder().name("Camel").product(cedarCoat).build()
            ));
            cedarCoat.setSizes(List.of(
                ProductSize.builder().name("S").product(cedarCoat).build(),
                ProductSize.builder().name("M").product(cedarCoat).build(),
                ProductSize.builder().name("L").product(cedarCoat).build(),
                ProductSize.builder().name("XL").product(cedarCoat).build(),
                ProductSize.builder().name("XXL").product(cedarCoat).build()
            ));
            cedarCoat.setStock(List.of(
                ProductStock.builder().size("S").quantity(4).product(cedarCoat).build(),
                ProductStock.builder().size("M").quantity(8).product(cedarCoat).build(),
                ProductStock.builder().size("L").quantity(7).product(cedarCoat).build(),
                ProductStock.builder().size("XL").quantity(5).product(cedarCoat).build(),
                ProductStock.builder().size("XXL").quantity(2).product(cedarCoat).build()
            ));
            products.add(cedarCoat);

            Product ashrafiehTrousers = Product.builder()
                    .name("Ashrafieh Slim Trousers").slug("ashrafieh-slim-trousers")
                    .description("Tapered trousers in premium stretch fabric — sharp enough for Gemmayzeh, comfortable enough for a long day.")
                    .price(new BigDecimal("110.00")).sku("AST-001")
                    .category(bottoms).collection(hamraNights).gender(Product.Gender.MEN)
                    .featured(false).newArrival(true).bestSeller(false).rating(4.3).reviewCount(8).build();
            
            ashrafiehTrousers.setImages(List.of(ProductImage.builder().url("https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600").product(ashrafiehTrousers).build()));
            ashrafiehTrousers.setColors(List.of(
                ProductColor.builder().name("Charcoal").product(ashrafiehTrousers).build(),
                ProductColor.builder().name("Stone").product(ashrafiehTrousers).build()
            ));
            ashrafiehTrousers.setSizes(List.of(
                ProductSize.builder().name("28").product(ashrafiehTrousers).build(),
                ProductSize.builder().name("30").product(ashrafiehTrousers).build(),
                ProductSize.builder().name("32").product(ashrafiehTrousers).build(),
                ProductSize.builder().name("34").product(ashrafiehTrousers).build(),
                ProductSize.builder().name("36").product(ashrafiehTrousers).build()
            ));
            ashrafiehTrousers.setStock(List.of(
                ProductStock.builder().size("28").quantity(6).product(ashrafiehTrousers).build(),
                ProductStock.builder().size("30").quantity(10).product(ashrafiehTrousers).build(),
                ProductStock.builder().size("32").quantity(12).product(ashrafiehTrousers).build(),
                ProductStock.builder().size("34").quantity(8).product(ashrafiehTrousers).build(),
                ProductStock.builder().size("36").quantity(4).product(ashrafiehTrousers).build()
            ));
            products.add(ashrafiehTrousers);

            Product sourDress = Product.builder()
                    .name("Sour Sunset Maxi Dress").slug("sour-sunset-maxi-dress")
                    .description("A flowing maxi dress in burnt orange linen, inspired by sunsets over the southern sea.")
                    .price(new BigDecimal("165.00")).sku("SSM-001")
                    .category(dresses).collection(medSummer).gender(Product.Gender.WOMEN)
                    .featured(true).newArrival(true).bestSeller(true).rating(4.7).reviewCount(19).build();
            
            sourDress.setImages(List.of(ProductImage.builder().url("https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?w=600").product(sourDress).build()));
            sourDress.setColors(List.of(
                ProductColor.builder().name("Sunset").product(sourDress).build(),
                ProductColor.builder().name("Sand").product(sourDress).build()
            ));
            sourDress.setSizes(List.of(
                ProductSize.builder().name("XS").product(sourDress).build(),
                ProductSize.builder().name("S").product(sourDress).build(),
                ProductSize.builder().name("M").product(sourDress).build(),
                ProductSize.builder().name("L").product(sourDress).build(),
                ProductSize.builder().name("XL").product(sourDress).build()
            ));
            sourDress.setStock(List.of(
                ProductStock.builder().size("XS").quantity(3).product(sourDress).build(),
                ProductStock.builder().size("S").quantity(7).product(sourDress).build(),
                ProductStock.builder().size("M").quantity(9).product(sourDress).build(),
                ProductStock.builder().size("L").quantity(6).product(sourDress).build(),
                ProductStock.builder().size("XL").quantity(4).product(sourDress).build()
            ));
            products.add(sourDress);

            productRepo.saveAll(products);

            // ─── Coupons ───────────────────────────────────────────────
            Coupon welcome = Coupon.builder()
                    .code("WELCOME15").discountType(Coupon.DiscountType.PERCENT)
                    .discountValue(new BigDecimal("15")).minOrderAmount(new BigDecimal("50"))
                    .usageLimit(500).usedCount(0).active(true).build();
            Coupon summer = Coupon.builder()
                    .code("SUMMER25").discountType(Coupon.DiscountType.PERCENT)
                    .discountValue(new BigDecimal("25")).minOrderAmount(new BigDecimal("150"))
                    .usageLimit(200).usedCount(0).active(true).build();
            Coupon fixed = Coupon.builder()
                    .code("CEDAR20").discountType(Coupon.DiscountType.FIXED)
                    .discountValue(new BigDecimal("20")).minOrderAmount(new BigDecimal("100"))
                    .usageLimit(0).usedCount(0).active(true).build();
            couponRepo.saveAll(List.of(welcome, summer, fixed));

            // ─── Banners ───────────────────────────────────────────────
            Banner b1 = Banner.builder()
                    .title("Mediterranean Summer").subtitle("Linen, sun, and sea")
                    .imageUrl("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1400")
                    .linkUrl("/collections/mediterranean-summer").displayOrder(1).active(true).build();
            Banner b2 = Banner.builder()
                    .title("Souk Heritage Collection").subtitle("Wear your roots with pride")
                    .imageUrl("https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=1400")
                    .linkUrl("/collections/souk-heritage").displayOrder(2).active(true).build();
            bannerRepo.saveAll(List.of(b1, b2));

            log.info("✅ Cedar & Co. seed data loaded successfully!");
        };
    }
}
