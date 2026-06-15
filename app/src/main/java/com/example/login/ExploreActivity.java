package com.example.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    // ── Data ──
    List<Book> allBooks = new ArrayList<>();
    List<Book> filteredBooks = new ArrayList<>();

    // ── Section lists ──
    List<Book> bestSellerBooks = new ArrayList<>();
    List<Book> newBooks = new ArrayList<>();
    List<Book> topRatedBooks = new ArrayList<>();
    List<Book> forYouBooks = new ArrayList<>();
    List<Book> podcastBooks = new ArrayList<>();

    // ── Adapters ──
    SectionBookAdapter bestSellerAdapter;
    SectionBookAdapter newBooksAdapter;
    SectionBookAdapter topRatedAdapter;
    SectionBookAdapter forYouAdapter;
    SectionBookAdapter podcastAdapter;

    // ── RecyclerViews ──
    RecyclerView rvBestSeller, rvNew, rvTopRated, rvForYou, rvPodcast;

    // ── Section title TextViews ──
    TextView tvSectionBestSeller, tvSectionNew, tvSectionTopRated,
             tvSectionForYou, tvSectionPodcast;
    LinearLayout sectionPodcast;

    // ── State ──
    String currentFilter = "All";
    String currentSearch = "";

    SupabaseBookService supabaseBookService;

    // ── Tên hiển thị cho từng filter key ──
    private static final String[][] FILTER_DATA = {
            {"All",          "Tất cả"},
            {"Audiobook",    "Sách nói"},
            {"Podcast",      "Podcast"},
            {"Meditation",   "Thiền định"},
            {"Book Summary", "Tóm tắt sách"},
            {"Favorite",     "Yêu thích"},
            {"Offline",      "Offline"}
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        supabaseBookService = new SupabaseBookService();

        bindViews();
        setupAdapters();
        setupChips();
        setupSearch();
        setupNavigation();

        loadBooks();
    }

    // ──────────────────────────────────────────────
    //  Setup
    // ──────────────────────────────────────────────

    private void bindViews() {
        rvBestSeller  = findViewById(R.id.rvExploreBestSeller);
        rvNew         = findViewById(R.id.rvExploreNew);
        rvTopRated    = findViewById(R.id.rvExploreTopRated);
        rvForYou      = findViewById(R.id.rvExploreForYou);
        rvPodcast     = findViewById(R.id.rvExplorePodcast);

        tvSectionBestSeller = findViewById(R.id.tvSectionBestSeller);
        tvSectionNew        = findViewById(R.id.tvSectionNew);
        tvSectionTopRated   = findViewById(R.id.tvSectionTopRated);
        tvSectionForYou     = findViewById(R.id.tvSectionForYou);
        tvSectionPodcast    = findViewById(R.id.tvSectionPodcast);
        sectionPodcast      = findViewById(R.id.sectionPodcast);
    }

    private void setupAdapters() {
        bestSellerAdapter = new SectionBookAdapter(bestSellerBooks);
        newBooksAdapter   = new SectionBookAdapter(newBooks);
        topRatedAdapter   = new SectionBookAdapter(topRatedBooks);
        forYouAdapter     = new SectionBookAdapter(forYouBooks);
        podcastAdapter    = new SectionBookAdapter(podcastBooks);

        bestSellerAdapter.setOnSectionBookClickListener(this::showBookDetail);
        newBooksAdapter.setOnSectionBookClickListener(this::showBookDetail);
        topRatedAdapter.setOnSectionBookClickListener(this::showBookDetail);
        forYouAdapter.setOnSectionBookClickListener(this::showBookDetail);
        podcastAdapter.setOnSectionBookClickListener(this::showBookDetail);

        setupHorizontal(rvBestSeller, bestSellerAdapter);
        setupHorizontal(rvNew,        newBooksAdapter);
        setupHorizontal(rvTopRated,   topRatedAdapter);
        setupHorizontal(rvForYou,     forYouAdapter);
        setupHorizontal(rvPodcast,    podcastAdapter);
    }

    private void setupHorizontal(RecyclerView rv, RecyclerView.Adapter<?> adapter) {
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etExploreSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                filterAndRefresh();
            }
        });

        // Filter button → dialog
        findViewById(R.id.btnExploreFilter).setOnClickListener(v -> showFilterDialog());
    }

    private void setupNavigation() {
        // Trang chủ
        findViewById(R.id.navHome).setOnClickListener(v -> {
            String username = getIntent().getStringExtra("username");
            Intent intent = new Intent(this, HomeActivity.class);
            if (username != null) intent.putExtra("username", username);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Thư viện
        findViewById(R.id.navLibrary).setOnClickListener(v -> {
            Intent libIntent = new Intent(this, LibraryActivity.class);
            if (getIntent().getStringExtra("username") != null)
                libIntent.putExtra("username", getIntent().getStringExtra("username"));
            startActivity(libIntent);
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            if (getIntent().getStringExtra("username") != null)
                profileIntent.putExtra("username", getIntent().getStringExtra("username"));
            startActivity(profileIntent);
            finish();
        });
    }

    // ──────────────────────────────────────────────
    //  Chip filter
    // ──────────────────────────────────────────────

    void setupChips() {
        LinearLayout chipGroup = findViewById(R.id.chipGroupExplore);
        chipGroup.removeAllViews();

        for (String[] pair : FILTER_DATA) {
            String key         = pair[0];
            String displayName = pair[1];

            TextView chip = new TextView(this);
            chip.setText(displayName);
            chip.setTextSize(13f);
            chip.setPadding(36, 14, 36, 14);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd(10);
            chip.setLayoutParams(lp);

            boolean isActive = key.equals(currentFilter);
            chip.setBackground(getDrawable(isActive ? R.drawable.chip_active : R.drawable.chip_inactive));
            chip.setTextColor(isActive ? 0xFFFFFFFF : 0xFFAAB3C5);
            if (isActive) chip.setTypeface(null, android.graphics.Typeface.BOLD);

            chip.setOnClickListener(v -> {
                currentFilter = key;
                setupChips();
                updateSectionTitles();
                filterAndRefresh();
            });

            chipGroup.addView(chip);
        }
    }

    // ──────────────────────────────────────────────
    //  Dynamic section titles
    // ──────────────────────────────────────────────

    void updateSectionTitles() {
        if (currentFilter.equals("All")) {
            tvSectionBestSeller.setText("♨  Sách bán chạy nhất mọi thời đại");
            tvSectionNew.setText("✦  Mới xuất bản");
            tvSectionTopRated.setText("☆  Đánh giá cao nhất");
            tvSectionForYou.setText("♥  Gợi ý cho bạn");
            tvSectionPodcast.setText("▣  Podcast & Tóm tắt sách");
            sectionPodcast.setVisibility(View.VISIBLE);
        } else {
            String name = getDisplayName(currentFilter);
            tvSectionBestSeller.setText("♨  " + name + " bán chạy");
            tvSectionNew.setText("✦  " + name + " mới nhất");
            tvSectionTopRated.setText("☆  " + name + " đánh giá cao");
            tvSectionForYou.setText("♥  Gợi ý " + name.toLowerCase() + " cho bạn");

            // Section 5: chỉ hiện khi lọc Podcast / Book Summary / All
            if (currentFilter.equals("Podcast") || currentFilter.equals("Book Summary")) {
                tvSectionPodcast.setText("▣  " + name + " nổi bật");
                sectionPodcast.setVisibility(View.VISIBLE);
            } else {
                sectionPodcast.setVisibility(View.GONE);
            }
        }
    }

    private String getDisplayName(String key) {
        for (String[] pair : FILTER_DATA) {
            if (pair[0].equals(key)) return pair[1];
        }
        return key;
    }

    // ──────────────────────────────────────────────
    //  Data loading & filtering
    // ──────────────────────────────────────────────

    private void loadBooks() {
        supabaseBookService.getBooks(new SupabaseBookService.BooksCallback() {
            @Override
            public void onSuccess(List<Book> books) {
                allBooks.clear();
                allBooks.addAll(books);
                filterAndRefresh();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ExploreActivity.this,
                        "Không tải được dữ liệu: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    void filterAndRefresh() {
        filteredBooks.clear();

        for (Book b : allBooks) {
            boolean matchType;

            switch (currentFilter) {
                case "All":      matchType = true;             break;
                case "Favorite": matchType = b.isFavorite();   break;
                case "Offline":  matchType = b.isDownloaded(); break;
                default:         matchType = safeEquals(b.getType(), currentFilter); break;
            }

            String kw = currentSearch.toLowerCase();
            boolean matchSearch = currentSearch.isEmpty()
                    || safeLower(b.getTitle()).contains(kw)
                    || safeLower(b.getAuthor()).contains(kw)
                    || safeLower(b.getGenre()).contains(kw)
                    || safeLower(b.getTopic()).contains(kw)
                    || safeLower(b.getType()).contains(kw);

            if (matchType && matchSearch) filteredBooks.add(b);
        }

        updateSections();
    }

    private void updateSections() {
        bestSellerBooks.clear();
        newBooks.clear();
        topRatedBooks.clear();
        forYouBooks.clear();
        podcastBooks.clear();

        List<Book> src = new ArrayList<>(filteredBooks);

        // Best seller: exclude Podcast khi filter=All, còn lại lấy hết đã filtered
        for (Book b : src) {
            if (currentFilter.equals("All") && safeEquals(b.getType(), "Podcast")) continue;
            bestSellerBooks.add(b);
        }
        sort(bestSellerBooks, false);

        // New: sắp xếp theo năm mới nhất
        newBooks.addAll(src);
        newBooks.sort((a, b) -> b.getYear() - a.getYear());

        // Top rated: sắp xếp theo rating
        topRatedBooks.addAll(src);
        sort(topRatedBooks, false);

        // For you: mixed từ filtered, ưu tiên rating cao
        forYouBooks.addAll(src);
        sort(forYouBooks, false);

        // Podcast section: chỉ lấy Podcast + Book Summary (khi filter=All / Podcast / Book Summary)
        if (currentFilter.equals("All")) {
            for (Book b : src) {
                if (safeEquals(b.getType(), "Podcast") || safeEquals(b.getType(), "Book Summary")) {
                    podcastBooks.add(b);
                }
            }
            if (podcastBooks.isEmpty()) podcastBooks.addAll(src);
        } else if (currentFilter.equals("Podcast") || currentFilter.equals("Book Summary")) {
            podcastBooks.addAll(src);
        }
        sort(podcastBooks, false);

        limit(bestSellerBooks, 10);
        limit(newBooks, 10);
        limit(topRatedBooks, 10);
        limit(forYouBooks, 10);
        limit(podcastBooks, 10);

        bestSellerAdapter.notifyDataSetChanged();
        newBooksAdapter.notifyDataSetChanged();
        topRatedAdapter.notifyDataSetChanged();
        forYouAdapter.notifyDataSetChanged();
        podcastAdapter.notifyDataSetChanged();
    }

    // ──────────────────────────────────────────────
    //  Book detail dialog
    // ──────────────────────────────────────────────

    private void showBookDetail(Book book) {
        String desc = (book.getDescription() == null || book.getDescription().trim().isEmpty())
                ? "Chưa có mô tả cho nội dung này."
                : book.getDescription();

        String audioStatus = (book.getAudioUrl() != null && !book.getAudioUrl().trim().isEmpty())
                ? "Đã có audio demo"
                : "Chưa có audio demo";

        String message = desc + "\n\n"
                + "Tác giả: " + book.getAuthor() + "\n"
                + "Thể loại: " + book.getGenre() + "\n"
                + "Loại: " + book.getType() + "\n"
                + "Chủ đề: " + book.getTopic() + "\n"
                + "Năm: " + book.getYear() + "\n"
                + "Thời lượng: " + fmt(book.getDuration()) + "\n"
                + "Tiến độ: " + fmt(book.getProgress()) + " / " + fmt(book.getDuration()) + "\n"
                + "Đánh giá: ★ " + String.format(java.util.Locale.US, "%.1f", book.getRating()) + "\n"
                + "Yêu thích: " + (book.isFavorite() ? "Có" : "Không") + "\n"
                + "Audio: " + audioStatus;

        new AlertDialog.Builder(this)
                .setTitle(book.getTitle())
                .setMessage(message)
                .setPositiveButton("Nghe thử", (d, w) -> playContent(book))
                .setNeutralButton(book.isFavorite() ? "Bỏ yêu thích" : "Yêu thích",
                        (d, w) -> toggleFavorite(book))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void toggleFavorite(Book book) {
        boolean newVal = !book.isFavorite();
        supabaseBookService.updateFavorite(book.getId(), newVal, new SupabaseBookService.UpdateCallback() {
            @Override
            public void onSuccess() {
                book.setFavorite(newVal);
                Toast.makeText(ExploreActivity.this,
                        newVal ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích",
                        Toast.LENGTH_SHORT).show();
                loadBooks();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(ExploreActivity.this,
                        "Không cập nhật được: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playContent(Book book) {
        if (book.getAudioUrl() == null || book.getAudioUrl().isEmpty()) {
            Toast.makeText(this, "Nội dung này chưa có file âm thanh!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Gửi lệnh phát nhạc tới Service
        Intent serviceIntent = new Intent(this, FonosAudioService.class);
        serviceIntent.putExtra("audio_url", book.getAudioUrl());
        serviceIntent.putExtra("title", book.getTitle());
        serviceIntent.putExtra("author", book.getAuthor());
        serviceIntent.putExtra("cover_url", book.getCoverUrl());
        startService(serviceIntent);

        // 2. Mở màn hình Player
        Intent playerIntent = new Intent(this, PlayerActivity.class);
        playerIntent.putExtra("cover_url", book.getCoverUrl());
        startActivity(playerIntent);
    }

    // ──────────────────────────────────────────────
    //  Filter dialog
    // ──────────────────────────────────────────────

    private void showFilterDialog() {
        String[] keys    = new String[FILTER_DATA.length];
        String[] names   = new String[FILTER_DATA.length];
        for (int i = 0; i < FILTER_DATA.length; i++) {
            keys[i]  = FILTER_DATA[i][0];
            names[i] = FILTER_DATA[i][1];
        }
        new AlertDialog.Builder(this)
                .setTitle("Lọc nội dung")
                .setItems(names, (d, which) -> {
                    currentFilter = keys[which];
                    setupChips();
                    updateSectionTitles();
                    filterAndRefresh();
                })
                .show();
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private void sort(List<Book> list, boolean ascending) {
        list.sort((a, b) -> ascending
                ? Double.compare(a.getRating(), b.getRating())
                : Double.compare(b.getRating(), a.getRating()));
    }

    private void limit(List<Book> list, int max) {
        while (list.size() > max) list.remove(list.size() - 1);
    }

    private boolean safeEquals(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String fmt(int seconds) {
        int m = seconds / 60, s = seconds % 60;
        return m + ":" + (s < 10 ? "0" : "") + s;
    }
}
