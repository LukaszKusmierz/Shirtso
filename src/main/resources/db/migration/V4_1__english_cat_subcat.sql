-- Update category names
UPDATE category SET category_name = 'Outerwear' WHERE category_name = 'Odzież wierzchnia';
UPDATE category SET category_name = 'Casual wear' WHERE category_name = 'Odzież casualowa';
UPDATE category SET category_name = 'Formal wear' WHERE category_name = 'Odzież elegancka';
UPDATE category SET category_name = 'Pants and shorts' WHERE category_name = 'Spodnie i szorty';
UPDATE category SET category_name = 'Underwear and sleepwear' WHERE category_name = 'Bielizna i odzież nocna';
UPDATE category SET category_name = 'Sportswear' WHERE category_name = 'Odzież sportowa';
UPDATE category SET category_name = 'Footwear' WHERE category_name = 'Obuwie';
UPDATE category SET category_name = 'Accessories' WHERE category_name = 'Akcesoria';

-- Update subcategory names - Outerwear (Category 1)
UPDATE subcategory SET subcategory_name = 'Jackets' WHERE subcategory_name = 'Kurtki';
UPDATE subcategory SET subcategory_name = 'Coats' WHERE subcategory_name = 'Płaszcze';
UPDATE subcategory SET subcategory_name = 'Blazers' WHERE subcategory_name = 'Marynarki';
UPDATE subcategory SET subcategory_name = 'Vests' WHERE subcategory_name = 'Kamizelki';
UPDATE subcategory SET subcategory_name = 'Parkas' WHERE subcategory_name = 'Parki';
UPDATE subcategory SET subcategory_name = 'Bomber jackets' WHERE subcategory_name = 'Bomberki';

-- Update subcategory names - Casual wear (Category 2)
UPDATE subcategory SET subcategory_name = 'T-shirts' WHERE subcategory_name = 'T-shirty';
UPDATE subcategory SET subcategory_name = 'Polo shirts' WHERE subcategory_name = 'Koszulki polo';
UPDATE subcategory SET subcategory_name = 'Casual shirts' WHERE subcategory_name = 'Koszule casualowe';
UPDATE subcategory SET subcategory_name = 'Sweatshirts' WHERE subcategory_name = 'Bluzy';
UPDATE subcategory SET subcategory_name = 'Sweaters' WHERE subcategory_name = 'Swetry';
UPDATE subcategory SET subcategory_name = 'Long sleeves' WHERE subcategory_name = 'Longsleeve''y';

-- Update subcategory names - Formal wear (Category 3)
UPDATE subcategory SET subcategory_name = 'Dress shirts' WHERE subcategory_name = 'Koszule wizytowe';
UPDATE subcategory SET subcategory_name = 'Suits' WHERE subcategory_name = 'Garnitury';
UPDATE subcategory SET subcategory_name = 'Formal blazers' WHERE subcategory_name = 'Marynarki eleganckie';
UPDATE subcategory SET subcategory_name = 'Suit vests' WHERE subcategory_name = 'Kamizelki garniturowe';
UPDATE subcategory SET subcategory_name = 'Dress pants' WHERE subcategory_name = 'Spodnie materiałowe';

-- Update subcategory names - Pants and shorts (Category 4)
UPDATE subcategory SET subcategory_name = 'Jeans' WHERE subcategory_name = 'Jeansy';
UPDATE subcategory SET subcategory_name = 'Chinos' WHERE subcategory_name = 'Spodnie chinosy';
UPDATE subcategory SET subcategory_name = 'Sweatpants' WHERE subcategory_name = 'Spodnie dresowe';
UPDATE subcategory SET subcategory_name = 'Cargo pants' WHERE subcategory_name = 'Spodnie cargo';
UPDATE subcategory SET subcategory_name = 'Dress trousers' WHERE subcategory_name = 'Spodnie garniturowe';
UPDATE subcategory SET subcategory_name = 'Denim shorts' WHERE subcategory_name = 'Szorty jeansowe';
UPDATE subcategory SET subcategory_name = 'Athletic shorts' WHERE subcategory_name = 'Szorty sportowe';
UPDATE subcategory SET subcategory_name = 'Casual shorts' WHERE subcategory_name = 'Szorty casualowe';

-- Update subcategory names - Underwear and sleepwear (Category 5)
UPDATE subcategory SET subcategory_name = 'Boxers' WHERE subcategory_name = 'Bokserki';
UPDATE subcategory SET subcategory_name = 'Briefs' WHERE subcategory_name = 'Slipki';
UPDATE subcategory SET subcategory_name = 'Long johns' WHERE subcategory_name = 'Kalesony';
UPDATE subcategory SET subcategory_name = 'Socks' WHERE subcategory_name = 'Skarpety';
UPDATE subcategory SET subcategory_name = 'Pajamas' WHERE subcategory_name = 'Piżamy';
UPDATE subcategory SET subcategory_name = 'Bathrobes' WHERE subcategory_name = 'Szlafroki';

-- Update subcategory names - Sportswear (Category 6)
UPDATE subcategory SET subcategory_name = 'Athletic shirts' WHERE subcategory_name = 'Koszulki sportowe';
UPDATE subcategory SET subcategory_name = 'Track pants' WHERE subcategory_name = 'Spodnie dresy';
UPDATE subcategory SET subcategory_name = 'Sports sweatshirts' WHERE subcategory_name = 'Bluzy sportowe';
UPDATE subcategory SET subcategory_name = 'Athletic leggings' WHERE subcategory_name = 'Legginsy sportowe';
UPDATE subcategory SET subcategory_name = 'Running gear' WHERE subcategory_name = 'Stroje do biegania';
UPDATE subcategory SET subcategory_name = 'Training gear' WHERE subcategory_name = 'Stroje treningowe';
UPDATE subcategory SET subcategory_name = 'Sports jackets' WHERE subcategory_name = 'Kurtki sportowe';

-- Update subcategory names - Footwear (Category 7)
UPDATE subcategory SET subcategory_name = 'Athletic shoes' WHERE subcategory_name = 'Buty sportowe';
UPDATE subcategory SET subcategory_name = 'Sneakers' WHERE subcategory_name = 'Sneakersy';
UPDATE subcategory SET subcategory_name = 'Loafers' WHERE subcategory_name = 'Mokasyny';
UPDATE subcategory SET subcategory_name = 'Dress shoes' WHERE subcategory_name = 'Buty eleganckie';
UPDATE subcategory SET subcategory_name = 'Canvas shoes' WHERE subcategory_name = 'Trampki';
UPDATE subcategory SET subcategory_name = 'Ankle boots' WHERE subcategory_name = 'Botki';
UPDATE subcategory SET subcategory_name = 'Sandals' WHERE subcategory_name = 'Sandały';

-- Update subcategory names - Accessories (Category 8)
UPDATE subcategory SET subcategory_name = 'Caps and hats' WHERE subcategory_name = 'Czapki i kapelusze';
UPDATE subcategory SET subcategory_name = 'Scarves' WHERE subcategory_name = 'Szaliki';
UPDATE subcategory SET subcategory_name = 'Gloves' WHERE subcategory_name = 'Rękawiczki';
UPDATE subcategory SET subcategory_name = 'Ties and bow ties' WHERE subcategory_name = 'Krawaty i muszki';
UPDATE subcategory SET subcategory_name = 'Belts' WHERE subcategory_name = 'Paski';
UPDATE subcategory SET subcategory_name = 'Wallets' WHERE subcategory_name = 'Portfele';
UPDATE subcategory SET subcategory_name = 'Sunglasses' WHERE subcategory_name = 'Okulary przeciwsłoneczne';
UPDATE subcategory SET subcategory_name = 'Bags and backpacks' WHERE subcategory_name = 'Torby i plecaki';
UPDATE subcategory SET subcategory_name = 'Watches' WHERE subcategory_name = 'Zegarki';
