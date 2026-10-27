package com.example.tripmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnExplore, btnPlan, btnTicketBooking, btnBudget, btnWeather, btnPacking, btnMyTrips;
    private MaterialCardView cardCoxsBazar;

    private static final String PREFS_NAME = "TripMatePrefs";
    private static final String KEY_TRIPS = "saved_trips_list";

    // Store planned trips
    private ArrayList<String> myTrips = new ArrayList<>();

    private AlertDialog.Builder createStyledBuilder() {
        return new AlertDialog.Builder(this, R.style.CustomDialogTheme);
    }

    private void styleAndShowDialog(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button neg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button neu = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            if (pos != null) {
                pos.setTextColor(0xFF7C3AED);
                pos.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            if (neg != null) {
                neg.setTextColor(0xFF7C3AED);
                neg.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            if (neu != null) {
                neu.setTextColor(0xFF7C3AED);
                neu.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        });
        dialog.show();
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openWebUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Opening URL: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    // =====================================
    // TRIP ITEM MODEL
    // =====================================

    static class TripItem {
        String destination;
        String days;
        String travelDate;
        String budget;
        String notes;
        String status; // "Upcoming ⏳" or "Completed ✅"

        TripItem(String destination, String days, String travelDate, String budget, String notes, String status) {
            this.destination = destination;
            this.days = days;
            this.travelDate = travelDate == null || travelDate.isEmpty() ? "TBD" : travelDate;
            this.budget = budget == null || budget.isEmpty() ? "0" : budget;
            this.notes = notes == null || notes.isEmpty() ? "None" : notes;
            this.status = status == null || status.isEmpty() ? "Upcoming ⏳" : status;
        }

        String serialize() {
            return destination + "~" + days + "~" + travelDate + "~" + budget + "~" + notes + "~" + status;
        }

        static TripItem deserialize(String raw) {
            String[] parts = raw.split("~", -1);
            if (parts.length >= 6) {
                return new TripItem(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
            } else if (parts.length >= 2) {
                return new TripItem(parts[0], parts[1], "TBD", "0", "None", "Upcoming ⏳");
            } else {
                return new TripItem(raw, "1", "TBD", "0", "None", "Upcoming ⏳");
            }
        }

        String getListSummary() {
            return status + " " + destination + " (" + days + " days)\n" +
                    "🗓 Date: " + travelDate + " | 💰 Budget: ৳" + budget;
        }

        String getFullDetailView() {
            return "📍 DESTINATION: " + destination + "\n\n" +
                    "⏱ DURATION: " + days + " days\n\n" +
                    "🗓 TRAVEL STARTING DATE: " + travelDate + "\n\n" +
                    "💰 ESTIMATED BUDGET: ৳" + budget + "\n\n" +
                    "📝 HOTEL & BOOKING NOTES:\n" + notes + "\n\n" +
                    "📊 TRIP STATUS: " + status;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load permanently saved trips
        loadSavedTrips();

        // Connect Buttons
        btnExplore = findViewById(R.id.btnExplore);
        btnPlan = findViewById(R.id.btnPlan);
        btnTicketBooking = findViewById(R.id.btnTicketBooking);
        btnBudget = findViewById(R.id.btnBudget);
        btnWeather = findViewById(R.id.btnWeather);
        btnPacking = findViewById(R.id.btnPacking);
        btnMyTrips = findViewById(R.id.btnMyTrips);

        // Connect Featured Destination Card
        cardCoxsBazar = findViewById(R.id.cardCoxsBazar);

        // Click Listeners
        btnExplore.setOnClickListener(v -> showDestinations());
        btnPlan.setOnClickListener(v -> showTripPlanner());
        btnTicketBooking.setOnClickListener(v -> showTicketBookingAssistant());
        btnBudget.setOnClickListener(v -> showBudgetCalculator());
        btnWeather.setOnClickListener(v -> showWeatherForecast());
        btnPacking.setOnClickListener(v -> showPackingChecklist());
        btnMyTrips.setOnClickListener(v -> showMyTrips());

        // Card Click Listener with Images
        cardCoxsBazar.setOnClickListener(v -> showDestinationDetail(
                R.drawable.coxsbazar,
                "Cox's Bazar",
                "🏖 World's Longest Natural Sea Beach (120 km) with golden sand and endless ocean views.",
                "• Marine Drive Scenic Route\n• Himchari Waterfall & Hilltop\n• Inani Coral Beach\n• Saint Martin's Island Coral Reef Tour\n• Radiant Fish World Aquarium",
                "• Fresh Coral Fish Fry\n• Loitta Fish Fry & Shutki Bhorta\n• Seafood BBQ at Laboni Beach",
                "• By AC/Non-AC Bus directly from Dhaka (8-10 hrs)\n• By Flight to Cox's Bazar Airport (CXB - 50 mins)\n• By Train via Cox's Bazar Iconic Railway Station",
                "November to March (Winter Season)",
                "৳5,000 - ৳12,000 / person (3 Days Tour)"
        ));
    }

    // =====================================
    // PERMANENT STORAGE (SharedPreferences)
    // =====================================

    private void loadSavedTrips() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String rawData = prefs.getString(KEY_TRIPS, "");
        myTrips.clear();

        if (!rawData.isEmpty()) {
            String[] items = rawData.split(";;;");
            for (String item : items) {
                if (!item.trim().isEmpty()) {
                    myTrips.add(item.trim());
                }
            }
        }
    }

    private void saveTripsToStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < myTrips.size(); i++) {

            sb.append(myTrips.get(i));

            if (i < myTrips.size() - 1) {
                sb.append(";;;");
            }
        }

        prefs.edit().putString(KEY_TRIPS, sb.toString()).apply();
    }

    // =====================================
    // 1. EXPLORE DESTINATIONS (CATEGORIZED)
    // =====================================

    private void showDestinations() {
        String[] categories = {
                "🌿 Natural & Eco-Tourism Spots",
                "🏰 Historical & Heritage Sites"
        };

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🌍 Explore Destinations Category")
                .setItems(categories, (dialog, which) -> {
                    if (which == 0) {
                        showNaturalDestinations();
                    } else {
                        showHistoricalDestinations();
                    }
                })
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    // --- Natural Destinations ---
    private void showNaturalDestinations() {
        String[] naturalSpots = {
                "🏖 Cox's Bazar (World's Longest Sea Beach)",
                "🌿 Sylhet & Sreemangal (Tea Gardens & Swamps)",
                "⛰ Bandarban (High Peaks & Waterfalls)",
                "☁ Sajek Valley (Queen of Hills & Clouds)",
                "⛵ Rangamati (Kaptai Lake & Hanging Bridge)",
                "🐅 Sundarbans (World's Largest Mangrove Forest)",
                "🌅 Kuakata (Sunrise & Sunset Sea Beach)"
        };

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🌿 Natural & Eco-Tourism Spots")
                .setItems(naturalSpots, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            cardCoxsBazar.performClick();
                            break;
                        case 1:
                            showDestinationDetail(
                                    R.drawable.sylhet_pic,
                                    "Sylhet & Sreemangal",
                                    "🌿 Spiritual Land of Lush Green Tea Gardens, Rivers & Freshwater Swamp Forests.",
                                    "• Jaflong & Zero Point River\n• Ratargul Freshwater Swamp Forest\n• Bichnakandi & Volagong White Stones\n• Lawachara National Park (Sreemangal)\n• Malnicherra & Hamham Waterfall",
                                    "• Seven-Layer Colored Tea (Sreemangal)\n• Shatkora Beef Curry\n• Fresh River Fish & Duck Bhuna",
                                    "• By Train (Upaban / Parabat Express - 6 hrs)\n• By Bus from Sayedabad / Fakirapool (5-6 hrs)\n• By Flight to Osmani International Airport (45 mins)",
                                    "October to March (Winter) & July to September (Monsoon for Swamps)",
                                    "৳4,500 - ৳10,000 / person (3 Days Tour)"
                            );
                            break;
                        case 2:
                            showDestinationDetail(
                                    R.drawable.img_bandarban,
                                    "Bandarban",
                                    "⛰ Majestic Mountain Ranges, Cloud Peaks, Tribal Culture & Thrilling Waterfalls.",
                                    "• Nilgiri Cloud Resort & Sunset Viewpoint\n• Boga Lake & Keokradong Peak Trekking\n• Amiakhum & Nafakhum Waterfalls\n• Golden Temple (Buddha Dhatu Jadi)\n• Chimbuk Hill & Shoilo Propat Waterfall",
                                    "• Bamboo Chicken (Chicken cooked in bamboo stem)\n• Tribal Jhum BBQ & Indigenous Fruits\n• Local Hill Banana & Papaya",
                                    "• By Bus from Dhaka to Bandarban Town (8-9 hrs)\n• By Chander Gari (4x4 Jeep) for local mountain transport",
                                    "September to February (Best for trekking & clear views)",
                                    "৳5,000 - ৳11,000 / person (3 Days Tour)"
                            );
                            break;
                        case 3:
                            showDestinationDetail(
                                    R.drawable.sajek_pic,
                                    "Sajek Valley",
                                    "☁ Queen of Hills Located in Rangamati, Surrounded by Ocean of Floating Clouds.",
                                    "• Konglak Para (Highest Point of Sajek)\n• Helipad Sunset & Sunrise Viewpoint\n• Ruilui Para Tribal Village\n• Risang Waterfall & Alutila Mystery Cave (Khagrachari)",
                                    "• Bamboo Chicken & Bamboo Tea\n• Tribal Jhum Rice & Pahari Hash (Duck Curry)\n• Fresh Passion Fruit Juice",
                                    "• Bus to Khagrachari (7-8 hrs), then Chander Gari (Jeep) with Army Escort to Sajek (2 hrs)",
                                    "September to March (Best for cloud views)",
                                    "৳6,000 - ৳12,000 / person (3 Days Tour)"
                            );
                            break;
                        case 4:
                            showDestinationDetail(
                                    R.drawable.rangamati_pic,
                                    "Rangamati",
                                    "⛵ 'Lake City of Bangladesh' - Picturesque district surrounded by Kaptai Lake, green hills, Chakma tribal heritage, and waterfalls.",
                                    "• Kaptai Lake Boat Cruise & Island Restaurants\n• Rangamati Hanging Bridge (Jhuling Pul)\n• Shuvolong Waterfall & Gorge\n• Rajbana Vihara Monastery\n• Chakma Royal Palace (Rajbari) & Tribal Cultural Museum",
                                    "• Fresh Kaptai Lake Tilapia & Kebba Fish Fry\n• Tribal Bamboo Chicken & Bamboo Shoots\n• Fresh Pineapple, Jackfruit & Hill Bananas",
                                    "• Direct AC/Non-AC Bus from Dhaka to Rangamati Town (7-8 hrs) or Bus from Chittagong (2.5 hrs)",
                                    "October to March (Pleasant weather for lake boat cruises)",
                                    "৳4,000 - ৳9,000 / person (2-3 Days Tour)"
                            );
                            break;
                        case 5:
                            showDestinationDetail(
                                    R.drawable.sunderban_pic,
                                    "Sundarbans",
                                    "🐅 World's Largest Halophytic Mangrove Forest & UNESCO World Heritage Site.",
                                    "• Katka Beach & Deer Watching Tower\n• Hiron Point (Tiger Reserve)\n• Dublar Char Island & Fishermen Village\n• Karamjal Wildlife Breeding Center\n• River Cruise Boat Safari",
                                    "• Fresh Sundarban Pure Honey\n• Fresh Crab Fry & Shrimp Curry\n• Sundarban River Fish Curry",
                                    "• Bus to Khulna / Mongla (7-8 hrs), then Launch / Boat Safari Tour",
                                    "November to February (Mild & pleasant weather for boat cruise)",
                                    "৳7,000 - ৳15,000 / person (3 Days Cruise)"
                            );
                            break;
                        case 6:
                            showDestinationDetail(
                                    R.drawable.coxsbazar,
                                    "Kuakata",
                                    "🌅 'Daughter of Ocean' - Unique beach where you can watch both Sunrise & Sunset over the Bay of Bengal.",
                                    "• Kuakata Sea Beach (Sunrise & Sunset Point)\n• Gangamati Reserve Forest & Red Crab Beach\n• Jhau Bon & Lemur Char\n• Misripara Buddhist Temple (100-yr old statue)\n• Rakhain Tribal Village & Handloom",
                                    "• Fresh Fried Crab & Hilsa Fish\n• Rakhain Tribal Dry Fish Dishes\n• Fresh Coconut Water",
                                    "• Direct AC Bus from Dhaka via Padma Bridge (6-7 hrs)",
                                    "October to March",
                                    "৳4,000 - ৳9,000 / person (2-3 Days Tour)"
                            );
                            break;
                    }
                })
                .setPositiveButton("Back", (dialog, which) -> showDestinations())
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    // --- Historical & Heritage Destinations ---
    private void showHistoricalDestinations() {
        String[] heritageSpots = {
                "🕌 Shat Gombuj Mosque (Bagerhat - UNESCO Site)",
                "🏛 Mahasthangarh Ancient Citadel (Bogura)",
                "🛕 Paharpur Somapura Mahavihara (Naogaon - UNESCO Site)",
                "🏯 Lalbagh Fort (Dhaka)",
                "🏛 Ahsan Manzil Pink Palace (Dhaka)",
                "🏰 Sonargaon Folk Art Museum (Narayanganj)",
                "🏛 Panam City / Panam Nagar (Narayanganj)",
                "🛕 Kantajew Terracotta Temple (Dinajpur)",
                "🏛 Shalban Vihara & Mainamati (Comilla)"
        };

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🏰 Historical & Heritage Sites")
                .setItems(heritageSpots, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Shat Gombuj Mosque (Bagerhat)",
                                    "🕌 15th-century UNESCO World Heritage site built by Saint Khan Jahan Ali. Famous for 77 domes and Tughlaq architectural brilliance.",
                                    "• Shat Gombuj Mosque Complex\n• Tomb of Khan Jahan Ali & Dighi Lake\n• Singair Mosque & Nine Dome Mosque\n• Bagerhat Museum",
                                    "• Bagerhat Famous Chui Jhal Beef\n• Local Fresh Coconut Water & Sweets",
                                    "• AC/Non-AC Bus from Dhaka directly to Bagerhat/Khulna (5-6 hrs via Padma Bridge)",
                                    "October to March (Cool & comfortable season)",
                                    "৳3,500 - ৳7,000 / person (1-2 Days Tour)"
                            );
                            break;
                        case 1:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Mahasthangarh Citadel (Bogura)",
                                    "🏛 One of the earliest urban archaeological sites in Bangladesh (3rd century BC). Served as the ancient provincial capital Pundranagara.",
                                    "• Mahasthangarh Citadel Wall & Govinda Bhita\n• Gokul Medh (Beula's Lakshindar Basor Ghar)\n• Parasuramer Prasad & Bairagir Bhita\n• Mahasthangarh Archaeological Museum",
                                    "• Bogura Famous Doi (Curd) & Katchagolla\n• Traditional North Bengal Thali",
                                    "• Bus or Train from Dhaka to Bogura (4-5 hrs), then local transport to Mahasthangarh",
                                    "October to March",
                                    "৳3,000 - ৳6,000 / person (1-2 Days Tour)"
                            );
                            break;
                        case 2:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Paharpur Somapura Mahavihara (Naogaon)",
                                    "🛕 8th-century Pala Dynasty Buddhist Monastery & UNESCO World Heritage Site. Largest single Buddhist Vihara south of the Himalayas.",
                                    "• Somapura Mahavihara Central Temple Compound\n• Paharpur Archaeological Museum\n• Satyaban Bhita & Gandhasvari Temple",
                                    "• Naogaon Fresh Sweets & North Bengal Fish Curry",
                                    "• Bus/Train to Joypurhat or Naogaon (5-6 hrs), then local transport to Paharpur",
                                    "October to March",
                                    "৳3,500 - ৳7,000 / person (2 Days Tour)"
                            );
                            break;
                        case 3:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Lalbagh Fort (Dhaka)",
                                    "🏯 17th-century Mughal Fortress complex started in 1678 AD by Mughal Prince Muhammad Azam during the reign of Emperor Aurangzeb.",
                                    "• Tomb of Pari Bibi (Iran Dukht)\n• Lalbagh Fort Mosque & Diwan-i-Aam Auditorium\n• Fort Museum & Mughal Secret Passages",
                                    "• Old Dhaka Famous Kacchi Biryani (Haji/Nanna/Sultan's)\n• Beauty Lassi & Falooda",
                                    "• Located in Lalbagh, Old Dhaka. Accessible by Metro Rail (DU Station) + Rickshaw / CNG",
                                    "Year-round (Best on clear winter days)",
                                    "৳1,000 - ৳2,000 / person (Day Tour)"
                            );
                            break;
                        case 4:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Ahsan Manzil Pink Palace (Dhaka)",
                                    "🏛 Historic official palace seat and residence of the Nawab of Dhaka, situated on the bank of Buriganga River in Old Dhaka.",
                                    "• Ahsan Manzil Grand Dome & Pink Palace Rooms\n• Nawab Family History Museum & Portraits\n• Buriganga Riverfront Promenade",
                                    "• Old Dhaka Bakarkhani with Special Tea\n• Fresh Malai Tea & Shahi Morog Polao",
                                    "• Located in Islampur, Old Dhaka near Sadarghat Launch Terminal",
                                    "Year-round",
                                    "৳1,000 - ৳2,000 / person (Day Tour)"
                            );
                            break;
                        case 5:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Sonargaon Folk Art Museum (Narayanganj)",
                                    "🏰 Sonargaon was the historic capital of Isa Khan's Bengal kingdom. Houses the Folk Art and Crafts Museum in Boro Sardar Bari Palace.",
                                    "• Folk Art and Crafts Museum (Zainul Abedin Folk Museum)\n• Restored Boro Sardar Bari Palace\n• Sonargaon Crafts Village & Wooden Crafts",
                                    "• Sonargaon Famous Sweet Curd & Sweets\n• Fresh Meghna River Fish Fry",
                                    "• Bus from Gulistan / Sayedabad to Mograpara Crossing, Sonargaon (1-1.5 hrs)",
                                    "October to March",
                                    "৳1,200 - ৳2,500 / person (Day Tour)"
                            );
                            break;
                        case 6:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Panam City / Panam Nagar (Narayanganj)",
                                    "🏛 19th-century colonial architectural brick mansion street known as the 'Ghost City' of Sonargaon.",
                                    "• Panam Nagar Historic Brick Mansion Walking Street\n• Neel Kuthi (Indigo Factory Ruins)\n• East & West Historic Mansion Buildings",
                                    "• Local Sonargaon Tea & Fresh Coconut Water",
                                    "• Located 10 mins from Sonargaon Folk Art Museum in Narayanganj",
                                    "October to March",
                                    "৳1,200 - ৳2,500 / person (Day Tour)"
                            );
                            break;
                        case 7:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Kantajew Terracotta Temple (Dinajpur)",
                                    "🛕 18th-century late-medieval Hindu temple famous for its intricate terracotta plaques depicting stories from Ramayana & Mahabharata.",
                                    "• Kantajew Temple & Nayabad Mosque\n• Ramsagar Dighi (Largest man-made lake in Bangladesh)\n• Dinajpur Rajbari Palace",
                                    "• Dinajpur Katari Bhog Rice & Litchi (In Season)\n• Local Sweet Sandesh",
                                    "• Train (Ekota / Drutojan Express) or AC Bus from Dhaka to Dinajpur (6-8 hrs)",
                                    "November to March",
                                    "৳4,500 - ৳9,000 / person (2 Days Tour)"
                            );
                            break;
                        case 8:
                            showDestinationDetail(
                                    R.drawable.img_banner_travel,
                                    "Shalban Vihara & Mainamati (Comilla)",
                                    "🏛 Ancient 8th to 12th-century Deva dynasty Buddhist archaeological complex, monastery, and museum.",
                                    "• Shalban Vihara Monastery Ruins\n• Mainamati Museum\n• Ananda Vihara & Itakhola Mura\n• Comilla War Cemetery\n• Dharmasagar Dighi",
                                    "• Comilla Famous Matri Bhandar Rasomalai\n• Fresh Local Sweets",
                                    "• Direct Bus from Sayedabad / Jatrabari to Comilla (2 hrs) or Train to Comilla Station",
                                    "Year-round (Best during winter)",
                                    "৳1,500 - ৳3,500 / person (Day Tour)"
                            );
                            break;
                    }
                })
                .setPositiveButton("Back", (dialog, which) -> showDestinations())
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    // =====================================
    // 2. DETAILED DESTINATION EXPLORER WITH IMAGE
    // =====================================

    private void showDestinationDetail(int imageResId, String name, String overview, String spots, String food, String transport, String bestTime, String cost) {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(35, 20, 35, 10);

        // Destination Image Header
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(imageResId);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        int heightPx = (int) (140 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, heightPx);
        imageParams.setMargins(0, 0, 0, 20);
        imageView.setLayoutParams(imageParams);

        // Scrollable Text Details
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);

        String details = "📌 OVERVIEW:\n" + overview + "\n\n" +
                "📍 MUST-VISIT ATTRACTIONS:\n" + spots + "\n\n" +
                "🍲 FAMOUS LOCAL FOODS:\n" + food + "\n\n" +
                "🚌 HOW TO REACH:\n" + transport + "\n\n" +
                "🗓 BEST TIME TO VISIT:\n" + bestTime + "\n\n" +
                "💰 ESTIMATED BUDGET:\n" + cost;

        textView.setText(details);
        textView.setTextSize(14);
        textView.setTextColor(0xFF172554);

        scrollView.addView(textView);

        dialogLayout.addView(imageView);
        dialogLayout.addView(scrollView);

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("📍 " + name + " Travel Guide")
                .setView(dialogLayout)
                .setPositiveButton("🧳 Plan Trip", (dialog, which) -> showTripPlanner(name))
                .setNeutralButton("🎟️ Book Ticket/Hotel", (dialog, which) -> showTicketBookingAssistant(name))
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    // =====================================
    // 3. TICKET & HOTEL BOOKING ASSISTANT
    // =====================================

    private void showTicketBookingAssistant() {
        showTicketBookingAssistant("Bangladesh");
    }

    private void showTicketBookingAssistant(String location) {
        String[] options = {
                "🚌 Bus Ticket Booking (Shohoz, Hanif, Shyamoli, Green Line)",
                "🚆 Train Ticket Booking (Bangladesh Railway E-Ticket)",
                "✈️ Flight Ticket Booking (Biman, US-Bangla, Air Astra)",
                "🏨 Hotel & Resort Reservation (GoZayaan, Booking.com, Helplines)"
        };

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🎟️ Booking Assistant: " + location)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showBusBookingOptions(location);
                            break;
                        case 1:
                            showTrainBookingOptions(location);
                            break;
                        case 2:
                            showFlightBookingOptions(location);
                            break;
                        case 3:
                            showHotelBookingOptions(location);
                            break;
                    }
                })
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    private void showBusBookingOptions(String location) {
        String info = "🚌 BUS TICKET BOOKING GUIDE:\n\n" +
                "• Top Operators: Hanif Enterprise, Shyamoli Paribahan, Green Line, ENA, London Express.\n\n" +
                "• Online Ticket Portals:\n" +
                "  1. Shohoz Bus: www.shohoz.com/bus-tickets\n" +
                "  2. BusBD: www.busbd.com.bd\n\n" +
                "• Average Fares for " + location + ":\n" +
                "  - Non-AC Economy: ৳550 - ৳850\n" +
                "  - AC Business Class: ৳1,200 - ৳2,000";

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🚌 Bus Booking: " + location)
                .setMessage(info)
                .setPositiveButton("🌐 Open Shohoz Bus", (dialog, which) -> openWebUrl("https://www.shohoz.com/bus-tickets"))
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    private void showTrainBookingOptions(String location) {
        String info = "🚆 BANGLADESH RAILWAY E-TICKET GUIDE:\n\n" +
                "• Official Online Ticket Portal:\n" +
                "  eticket.railway.gov.bd\n\n" +
                "• Popular Train Seat Classes:\n" +
                "  - AC_B (AC Berth / Sleeper)\n" +
                "  - F_BERTH (First Class Berth)\n" +
                "  - SNIGDHA (AC Chair)\n" +
                "  - S_CHAIR (Non-AC Shovan Chair)\n\n" +
                "• Advance Ticket Opening:\n" +
                "  Tickets open online 10 days before travel date at 8:00 AM.";

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🚆 Train Booking: " + location)
                .setMessage(info)
                .setPositiveButton("🌐 Open Railway E-Ticket", (dialog, which) -> openWebUrl("https://eticket.railway.gov.bd"))
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    private void showFlightBookingOptions(String location) {
        String info = "✈️ DOMESTIC FLIGHT BOOKING GUIDE:\n\n" +
                "• Domestic Airlines in Bangladesh:\n" +
                "  1. Biman Bangladesh Airlines\n" +
                "  2. US-Bangla Airlines\n" +
                "  3. Air Astra\n" +
                "  4. Novoair\n\n" +
                "• Domestic Airport Codes:\n" +
                "  - Cox's Bazar (CXB)\n" +
                "  - Sylhet (ZYL)\n" +
                "  - Saidpur / Dinajpur (SPD)\n" +
                "  - Chittagong (CGP)\n" +
                "  - Barisal / Kuakata (BZL)";

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("✈️ Domestic Flights: " + location)
                .setMessage(info)
                .setPositiveButton("🌐 Biman Bangladesh", (dialog, which) -> openWebUrl("https://www.biman-airlines.com"))
                .setNeutralButton("🌐 US-Bangla", (dialog, which) -> openWebUrl("https://usbair.com"))
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    private void showHotelBookingOptions(String location) {
        String info = "🏨 HOTEL & RESORT RESERVATION:\n\n" +
                "• Online Hotel Booking Portals:\n" +
                "  - GoZayaan: www.gozayaan.com\n" +
                "  - ShareTrip: www.sharetrip.net\n" +
                "  - Booking.com\n\n" +
                "• Top Destinations Recommended Stays:\n" +
                "  - Cox's Bazar: Ocean Paradise, Long Beach, Seagull\n" +
                "  - Sajek Valley: Ruilui Resort, Meghkabya Hilltop\n" +
                "  - Bandarban: Nilgiri Cloud Resort, Hotel Plaza\n" +
                "  - Sylhet: Grand Sultan, Nazimgarh Resort";

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🏨 Hotel Reservation: " + location)
                .setMessage(info)
                .setPositiveButton("🌐 Open GoZayaan", (dialog, which) -> openWebUrl("https://www.gozayaan.com"))
                .setNeutralButton("🌐 Open Booking.com", (dialog, which) -> openWebUrl("https://www.booking.com"))
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    // =====================================
    // 4. EXPANDED TRIP PLANNER (CREATE & EDIT)
    // =====================================

    private void showTripPlanner() {
        showTripPlanner("", -1);
    }

    private void showTripPlanner(String selectedDestination) {
        showTripPlanner(selectedDestination, -1);
    }

    private void showTripPlanner(String selectedDestination, int editIndex) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        // Existing trip data if editing
        TripItem existingItem = null;
        if (editIndex >= 0 && editIndex < myTrips.size()) {
            existingItem = TripItem.deserialize(myTrips.get(editIndex));
        }

        EditText destinationInput = new EditText(this);
        destinationInput.setHint("Destination (e.g. Cox's Bazar)");
        destinationInput.setText(existingItem != null ? existingItem.destination : selectedDestination);

        EditText daysInput = new EditText(this);
        daysInput.setHint("Duration in days (e.g. 3)");
        daysInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        daysInput.setText(existingItem != null ? existingItem.days : "");

        EditText dateInput = new EditText(this);
        dateInput.setHint("Travel Date (e.g. 15 Oct 2026)");
        dateInput.setText(existingItem != null ? existingItem.travelDate : "");

        EditText budgetInput = new EditText(this);
        budgetInput.setHint("Budget in BDT (e.g. 8500)");
        budgetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        budgetInput.setText(existingItem != null ? existingItem.budget : "");

        EditText notesInput = new EditText(this);
        notesInput.setHint("Hotel & Booking Notes (e.g. Hotel Sea Palace)");
        notesInput.setText(existingItem != null ? existingItem.notes : "");

        layout.addView(destinationInput);
        layout.addView(daysInput);
        layout.addView(dateInput);
        layout.addView(budgetInput);
        layout.addView(notesInput);

        final TripItem currentItemToEdit = existingItem;

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle(editIndex >= 0 ? "✏️ Edit Saved Trip" : "🧳 Plan & Save Trip")
                .setMessage("Fill in your travel itinerary details:")
                .setView(layout)
                .setPositiveButton("Save Trip", (dialog, which) -> {
                    String destination = destinationInput.getText().toString().trim();
                    String days = daysInput.getText().toString().trim();
                    String date = dateInput.getText().toString().trim();
                    String budget = budgetInput.getText().toString().trim();
                    String notes = notesInput.getText().toString().trim();

                    if (destination.isEmpty() || days.isEmpty()) {
                        Toast.makeText(this, "Please enter destination & duration!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String status = currentItemToEdit != null ? currentItemToEdit.status : "Upcoming ⏳";
                    TripItem newTrip = new TripItem(destination, days, date, budget, notes, status);

                    if (editIndex >= 0 && editIndex < myTrips.size()) {
                        myTrips.set(editIndex, newTrip.serialize());
                        Toast.makeText(this, "Trip Updated Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        myTrips.add(newTrip.serialize());
                        Toast.makeText(this, "Trip Saved Permanently!", Toast.LENGTH_SHORT).show();
                    }

                    saveTripsToStorage();
                })
                .setNegativeButton("Cancel", null);

        styleAndShowDialog(builder);
    }

    // =====================================
    // 5. ENHANCED BUDGET CALCULATOR
    // =====================================

    private void showBudgetCalculator() {
        String[] options = {
                "💵 ৳5,000 Budget Breakdown",
                "💵 ৳10,000 Budget Breakdown",
                "💵 ৳15,000 Budget Breakdown",
                "💵 ৳20,000 Budget Breakdown",
                "🔢 Custom Amount Breakdown",
                "👥 Calculate By Persons & Days"
        };

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("💰 Travel Budget Calculator")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            calculateAndShowBreakdown(5000);
                            break;
                        case 1:
                            calculateAndShowBreakdown(10000);
                            break;
                        case 2:
                            calculateAndShowBreakdown(15000);
                            break;
                        case 3:
                            calculateAndShowBreakdown(20000);
                            break;
                        case 4:
                            showCustomBudgetInput();
                            break;
                        case 5:
                            showPersonDaysBudgetCalculator();
                            break;
                    }
                })
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    private void showCustomBudgetInput() {
        EditText budgetInput = new EditText(this);
        budgetInput.setHint("Enter amount in BDT (e.g. 12000)");
        budgetInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🔢 Enter Custom Budget Amount")
                .setMessage("Enter total amount for automatic category breakdown:")
                .setView(budgetInput)
                .setPositiveButton("Calculate", (dialog, which) -> {
                    String value = budgetInput.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(this, "Please enter an amount!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        long amount = Long.parseLong(value);
                        if (amount <= 0) {
                            Toast.makeText(this, "Amount must be greater than 0!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        calculateAndShowBreakdown(amount);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter a valid amount!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        styleAndShowDialog(builder);
    }

    private void showPersonDaysBudgetCalculator() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText personsInput = new EditText(this);
        personsInput.setHint("Number of Travelers/Persons (e.g. 2)");
        personsInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText daysInput = new EditText(this);
        daysInput.setHint("Number of Days (e.g. 3)");
        daysInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText dailyCostInput = new EditText(this);
        dailyCostInput.setHint("Est. Daily Cost per Person (e.g. 2500)");
        dailyCostInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(personsInput);
        layout.addView(daysInput);
        layout.addView(dailyCostInput);

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("👥 Calculate By Travelers & Duration")
                .setMessage("Enter travelers and daily budget details:")
                .setView(layout)
                .setPositiveButton("Calculate", (dialog, which) -> {
                    String pVal = personsInput.getText().toString().trim();
                    String dVal = daysInput.getText().toString().trim();
                    String cVal = dailyCostInput.getText().toString().trim();

                    if (pVal.isEmpty() || dVal.isEmpty() || cVal.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        long persons = Long.parseLong(pVal);
                        long days = Long.parseLong(dVal);
                        long dailyCost = Long.parseLong(cVal);

                        if (persons <= 0 || days <= 0 || dailyCost <= 0) {
                            Toast.makeText(this, "Values must be greater than 0!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long totalBudget = persons * days * dailyCost;
                        calculateAndShowBreakdown(totalBudget, persons, days);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter valid numbers!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        styleAndShowDialog(builder);
    }

    private void calculateAndShowBreakdown(long totalBudget) {
        calculateAndShowBreakdown(totalBudget, 1, 1);
    }

    private void calculateAndShowBreakdown(long totalBudget, long persons, long days) {
        long transport = (long) (totalBudget * 0.30);
        long hotel = (long) (totalBudget * 0.35);
        long food = (long) (totalBudget * 0.20);
        long entertainment = (long) (totalBudget * 0.10);
        long local = (long) (totalBudget * 0.05);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_budget_breakdown, null);

        DonutChartView donutChart = dialogView.findViewById(R.id.donutChartView);
        TextView tvTravelersInfo = dialogView.findViewById(R.id.tvTravelersDurationInfo);
        TextView tvTransport = dialogView.findViewById(R.id.tvTransportAmount);
        TextView tvHotel = dialogView.findViewById(R.id.tvHotelAmount);
        TextView tvFood = dialogView.findViewById(R.id.tvFoodAmount);
        TextView tvEntertainment = dialogView.findViewById(R.id.tvEntertainmentAmount);
        TextView tvLocal = dialogView.findViewById(R.id.tvLocalAmount);

        if (donutChart != null) {
            donutChart.setBudgetData(totalBudget, transport, hotel, food, entertainment, local);
        }

        if (tvTravelersInfo != null) {
            tvTravelersInfo.setText("👥 " + persons + " Traveler(s) • ⏱ " + days + " Day(s)");
        }
        if (tvTransport != null) {
            tvTransport.setText("৳ " + String.format("%,d", transport));
        }
        if (tvHotel != null) {
            tvHotel.setText("৳ " + String.format("%,d", hotel));
        }
        if (tvFood != null) {
            tvFood.setText("৳ " + String.format("%,d", food));
        }
        if (tvEntertainment != null) {
            tvEntertainment.setText("৳ " + String.format("%,d", entertainment));
        }
        if (tvLocal != null) {
            tvLocal.setText("৳ " + String.format("%,d", local));
        }

        AlertDialog.Builder builder = createStyledBuilder()
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .setNegativeButton("Recalculate", (dialog, which) -> showBudgetCalculator());

        styleAndShowDialog(builder);
    }

    // =====================================
    // 6. LIVE REAL-TIME WEATHER FORECAST
    // =====================================

    static class LocationCoords {
        double lat;
        double lon;
        String tip;
        String climateDesc;

        LocationCoords(double lat, double lon, String tip, String climateDesc) {
            this.lat = lat;
            this.lon = lon;
            this.tip = tip;
            this.climateDesc = climateDesc;
        }
    }

    private LocationCoords getLocationCoords(String location) {
        if (location.contains("Cox")) {
            return new LocationCoords(21.4272, 92.0058, "Perfect weather for swimming & beach walks! Don't forget sunscreen.", "🌡 26°C - 30°C | Sunny Sea Breeze");
        } else if (location.contains("Sylhet") || location.contains("Sreemangal")) {
            return new LocationCoords(24.8949, 91.8687, "Great conditions for exploring tea gardens & Ratargul swamp forest!", "🌡 22°C - 27°C | Pleasant Tea Garden Breeze");
        } else if (location.contains("Bandarban")) {
            return new LocationCoords(22.1950, 92.2182, "Ideal weather for mountain trekking to Nilgiri & Keokradong.", "🌡 19°C - 25°C | Cool Mountain Climate");
        } else if (location.contains("Sajek")) {
            return new LocationCoords(23.3820, 92.2938, "Early morning offers breathtaking cloud-sea views from Konglak Para!", "🌡 18°C - 24°C | Floating Clouds Climate");
        } else if (location.contains("Rangamati")) {
            return new LocationCoords(22.6533, 92.1789, "Pleasant weather for Kaptai Lake boat cruises & hanging bridge.", "🌡 22°C - 28°C | Kaptai Lake Breeze");
        } else if (location.contains("Sundarban")) {
            return new LocationCoords(22.4833, 89.6000, "Ideal conditions for river boat safaris and forest exploration.", "🌡 21°C - 27°C | Mangrove Forest Climate");
        } else if (location.contains("Kuakata")) {
            return new LocationCoords(21.8167, 90.1167, "Great time for early morning sunrise and evening sunset at the beach.", "🌡 24°C - 29°C | Coastal Ocean Breeze");
        } else if (location.contains("Bagerhat") || location.contains("Shat Gombuj")) {
            return new LocationCoords(22.6515, 89.7859, "Great weather for exploring UNESCO heritage mosque complex.", "🌡 23°C - 28°C | Mild Heritage Climate");
        } else if (location.contains("Bogura") || location.contains("Mahasthangarh") || location.contains("Paharpur")) {
            return new LocationCoords(24.8465, 89.3777, "Pleasant weather for exploring ancient citadel ruins & Paharpur Vihara.", "🌡 22°C - 27°C | Dry Citadel Climate");
        } else if (location.contains("Dhaka") || location.contains("Lalbagh")) {
            return new LocationCoords(23.8103, 90.4125, "Great weather for Old Dhaka heritage walks & Lalbagh Fort tour.", "🌡 24°C - 29°C | Urban Heritage Climate");
        } else if (location.contains("Sonargaon") || location.contains("Panam")) {
            return new LocationCoords(23.6238, 90.5000, "Pleasant conditions for visiting Panam Ghost City & Folk Museum.", "🌡 23°C - 28°C | Historic River Climate");
        } else if (location.contains("Dinajpur") || location.contains("Kantajew")) {
            return new LocationCoords(25.6279, 88.6332, "Great weather for viewing terracotta temple art and Ramsagar lake.", "🌡 20°C - 26°C | Pleasant North Bengal Climate");
        } else if (location.contains("Comilla") || location.contains("Shalban") || location.contains("Mainamati")) {
            return new LocationCoords(23.4607, 91.1809, "Good conditions for exploring Shalban Vihara & Comilla War Cemetery.", "🌡 23°C - 28°C | Mild Heritage Climate");
        } else {
            return new LocationCoords(23.8103, 90.4125, "Great weather for sightseeing, photography, and travel!", "🌡 24°C - 28°C | Pleasant Travel Climate");
        }
    }

    private String decodeWeatherCode(int code) {
        switch (code) {
            case 0:
                return "☀️ Clear Sky & Sunny";
            case 1:
            case 2:
                return "🌤 Partly Cloudy & Mild";
            case 3:
                return "☁️ Overcast & Cloudy";
            case 45:
            case 48:
                return "🌫 Foggy & Misty";
            case 51:
            case 53:
            case 55:
                return "🌧 Light Drizzle";
            case 61:
            case 63:
            case 65:
                return "🌧 Rain Shower";
            case 80:
            case 81:
            case 82:
                return "🌦 Heavy Rain Showers";
            case 95:
            case 96:
            case 99:
                return "⛈ Thunderstorm & Lightning";
            default:
                return "🌤 Pleasant & Clear Weather";
        }
    }

    private void showWeatherForecast() {
        String[] destinations = {
                "Cox's Bazar",
                "Sylhet",
                "Bandarban",
                "Sajek Valley",
                "Rangamati",
                "Sundarbans",
                "Kuakata",
                "Bagerhat (Shat Gombuj)",
                "Bogura (Mahasthangarh)",
                "Old Dhaka (Lalbagh Fort)",
                "Sonargaon & Panam City",
                "Dinajpur (Kantajew Temple)",
                "Comilla (Shalban Vihara)"
        };

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🌤 Select Destination for Weather")
                .setItems(destinations, (dialog, which) -> showWeatherForLocation(destinations[which]))
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    private void showWeatherForLocation(String location) {
        LocationCoords coords = getLocationCoords(location);

        // Check Network Connection for Instant 100% Offline Support
        if (!isNetworkAvailable()) {
            String offlineReport = "📍 LOCATION: " + location + "\n\n" +
                    "📶 MODE: Offline Climate Guide\n" +
                    "🌤 CLIMATE: " + coords.climateDesc + "\n" +
                    "☁️ CONDITION: Pleasant Seasonal Travel Weather\n\n" +
                    "💡 TRAVEL ADVICE:\n" + coords.tip;

            AlertDialog.Builder offlineBuilder = createStyledBuilder()
                    .setTitle("🌤 Seasonal Weather: " + location)
                    .setMessage(offlineReport)
                    .setPositiveButton("🧳 Plan Trip Here", (d, w) -> showTripPlanner(location))
                    .setNegativeButton("Close", null);

            styleAndShowDialog(offlineBuilder);
            return;
        }

        AlertDialog.Builder loadingBuilder = createStyledBuilder()
                .setTitle("🌤 Fetching Live Weather...")
                .setMessage("Connecting to live weather API for " + location + "...")
                .setCancelable(false);

        AlertDialog loadingDialog = loadingBuilder.create();
        loadingDialog.setOnShowListener(d -> {
            Button pos = loadingDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button neg = loadingDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button neu = loadingDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            if (pos != null) { pos.setTextColor(0xFF7C3AED); pos.setTypeface(null, android.graphics.Typeface.BOLD); }
            if (neg != null) { neg.setTextColor(0xFF7C3AED); neg.setTypeface(null, android.graphics.Typeface.BOLD); }
            if (neu != null) { neu.setTextColor(0xFF7C3AED); neu.setTypeface(null, android.graphics.Typeface.BOLD); }
        });
        loadingDialog.show();

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + coords.lat +
                        "&longitude=" + coords.lon + "&current_weather=true";

                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    org.json.JSONObject json = new org.json.JSONObject(sb.toString());
                    org.json.JSONObject currentWeather = json.getJSONObject("current_weather");

                    double temp = currentWeather.getDouble("temperature");
                    double wind = currentWeather.getDouble("windspeed");
                    int code = currentWeather.getInt("weathercode");

                    String condition = decodeWeatherCode(code);

                    String liveReport = "📍 LOCATION: " + location + "\n\n" +
                            "🌐 MODE: Live REST Weather API\n" +
                            "🌡 LIVE TEMPERATURE: " + temp + "°C\n" +
                            "☁️ CONDITION: " + condition + "\n" +
                            "💨 WIND SPEED: " + wind + " km/h\n\n" +
                            "💡 TRAVEL ADVICE:\n" + coords.tip;

                    runOnUiThread(() -> {
                        if (loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                        AlertDialog.Builder reportBuilder = createStyledBuilder()
                                .setTitle("🌤 Live Weather: " + location)
                                .setMessage(liveReport)
                                .setPositiveButton("🧳 Plan Trip Here", (d, w) -> showTripPlanner(location))
                                .setNegativeButton("Close", null);

                        styleAndShowDialog(reportBuilder);
                    });
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Fallback if network timeout
            runOnUiThread(() -> {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                String fallbackReport = "📍 LOCATION: " + location + "\n\n" +
                        "📶 MODE: Offline Climate Guide\n" +
                        "🌤 CLIMATE: " + coords.climateDesc + "\n" +
                        "☁️ CONDITION: Pleasant Seasonal Travel Weather\n\n" +
                        "💡 TRAVEL ADVICE:\n" + coords.tip;

                AlertDialog.Builder fallbackBuilder = createStyledBuilder()
                        .setTitle("🌤 Seasonal Weather: " + location)
                        .setMessage(fallbackReport)
                        .setPositiveButton("🧳 Plan Trip Here", (d, w) -> showTripPlanner(location))
                        .setNegativeButton("Close", null);

                styleAndShowDialog(fallbackBuilder);
            });
        });
    }

    // =====================================
    // 7. PACKING CHECKLIST FEATURE
    // =====================================

    private void showPackingChecklist() {
        String[] items = {
                "📄 NID Card / Student ID / Passport",
                "📱 Phone Charger & Power Bank",
                "👕 Season-appropriate Clothes & Towel",
                "💵 Cash & Debit/Credit Cards",
                "💊 First Aid Box & Necessary Medicines",
                "🕶 Sunglasses, Sunscreen & Umbrella",
                "📸 Camera & Memory Cards",
                "🧴 Toiletries (Toothbrush, Soap, Shampoo)"
        };

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean[] checkedItems = new boolean[items.length];

        for (int i = 0; i < items.length; i++) {

            checkedItems[i] = prefs.getBoolean("check_item_" + i, false);

        }

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("🧰 Travel Packing Checklist")
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                    prefs.edit().putBoolean("check_item_" + which, isChecked).apply();
                })
                .setPositiveButton("Done", (dialog, which) ->
                        Toast.makeText(this, "Checklist Saved!", Toast.LENGTH_SHORT).show()
                )
                .setNegativeButton("Close", null);

        styleAndShowDialog(builder);
    }

    // =====================================
    // 8. UPGRADED MY SAVED TRIPS MANAGER
    // =====================================

    private void showMyTrips() {
        if (myTrips.isEmpty()) {
            AlertDialog.Builder builder = createStyledBuilder()
                    .setTitle("📍 My Saved Trips")
                    .setMessage("No trips saved yet.\nPlan your first adventure!")
                    .setPositiveButton("Plan a Trip", (dialog, which) -> showTripPlanner())
                    .setNegativeButton("Close", null);

            styleAndShowDialog(builder);
        } else {
            String[] displayList = new String[myTrips.size()];
            for (int i = 0; i < myTrips.size(); i++) {
                TripItem trip = TripItem.deserialize(myTrips.get(i));
                displayList[i] = (i + 1) + ". " + trip.getListSummary();
            }

            AlertDialog.Builder builder = createStyledBuilder()
                    .setTitle("📍 My Saved Trips (" + myTrips.size() + ")")
                    .setItems(displayList, (dialog, which) -> showTripDetailDialog(which))
                    .setPositiveButton("Plan New", (dialog, which) -> showTripPlanner())
                    .setNeutralButton("Clear All", (dialog, which) -> {
                        myTrips.clear();
                        saveTripsToStorage();
                        Toast.makeText(this, "All trips cleared!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Close", null);

            styleAndShowDialog(builder);
        }
    }

    private void showTripDetailDialog(int index) {
        if (index < 0 || index >= myTrips.size()) return;

        TripItem trip = TripItem.deserialize(myTrips.get(index));

        AlertDialog.Builder builder = createStyledBuilder()
                .setTitle("📍 " + trip.destination + " Trip Details")
                .setMessage(trip.getFullDetailView())
                .setPositiveButton("✏️ Edit Trip", (dialog, which) -> showTripPlanner(trip.destination, index))
                .setNeutralButton(trip.status.contains("Completed") ? "Mark Upcoming ⏳" : "Mark Completed ✅", (dialog, which) -> {
                    if (trip.status.contains("Completed")) {
                        trip.status = "Upcoming ⏳";
                    } else {
                        trip.status = "Completed ✅";
                    }
                    myTrips.set(index, trip.serialize());
                    saveTripsToStorage();
                    Toast.makeText(this, "Trip Status Updated!", Toast.LENGTH_SHORT).show();
                    showMyTrips();
                })
                .setNegativeButton("🗑 Delete Trip", (dialog, which) -> {
                    myTrips.remove(index);
                    saveTripsToStorage();
                    Toast.makeText(this, "Trip Deleted!", Toast.LENGTH_SHORT).show();
                    showMyTrips();
                });

        styleAndShowDialog(builder);
    }
}