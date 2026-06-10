package com.example.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    // ── Data ──
    List<Book> allBooks      = new ArrayList<>();
    List<Book> filteredBooks = new ArrayList<>();

    // ── Adapter ──
    BookAdapter adapter;
    RecyclerView rvLibrary;

    // ── State ──
    String  currentFilter = "All";
    String  currentSearch = "";
    boolean isGrid        = true;

    SupabaseBookService supabaseBookService;

    private static final String[][] FILTER_DATA = {
            {"All",          "Tất cả"},
            {"Audiobook",    "Sách nói"},
            {"Podcast",      "Podcast"},
            {"Meditation",   "Thiền định"},
            {"Book Summary", "Tóm tắt sách"},
            {"Favorite",     "Yêu thích"},
            {"Offline",      "Offline"},
            {"History",      "Lịch sử"}
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        supabaseBookService = new SupabaseBookService();
        String initialFilter = getIntent().getStringExtra("initial_filter");
        if (isSupportedFilter(initialFilter)) currentFilter = initialFilter;

        rvLibrary = findViewById(R.id.rvLibrary);
        adapter   = new BookAdapter(filteredBooks, true);
        rvLibrary.setLayoutManager(new GridLayoutManager(this, 2));
        rvLibrary.setAdapter(adapter);

        setupBookActions();
        setupSearch();
        setupGridListToggle();
        setupChips();
        setupNavigation();

        loadBooks();
    }

    // ──────────────────────────────────────────────
    //  Book action callbacks
    // ──────────────────────────────────────────────

    private void setupBookActions() {
        adapter.setOnBookActionListener(new BookAdapter.OnBookActionListener() {
            @Override
            public void onPlay(Book book) { showBookDetail(book); }

            @Override
            public void onFavorite(Book book) { toggleFavorite(book); }

            @Override
            public void onDownload(Book book) {
                Toast.makeText(LibraryActivity.this,
                        "Download offline — sắp có", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEdit(Book book) {
                Toast.makeText(LibraryActivity.this,
                        "Chỉnh sửa — sắp có", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelete(Book book) {
                Toast.makeText(LibraryActivity.this,
                        "Xóa — sắp có", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Search
    // ──────────────────────────────────────────────

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etLibrarySearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                currentSearch = s.toString().trim();
                filterAndSort();
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Grid / List toggle
    // ──────────────────────────────────────────────

    private void setupGridListToggle() {
        Button btnGrid = findViewById(R.id.btnLibraryGrid);
        Button btnList = findViewById(R.id.btnLibraryList);

        btnGrid.setOnClickListener(v -> {
            if (!isGrid) {
                isGrid = true;
                adapter.setGridMode(true);
                rvLibrary.setLayoutManager(new GridLayoutManager(this, 2));
                btnGrid.setBackgroundTintList(ColorStateList.valueOf(0xFF667EEA));
                btnList.setBackgroundTintList(ColorStateList.valueOf(0xFF1B2D55));
            }
        });

        btnList.setOnClickListener(v -> {
            if (isGrid) {
                isGrid = false;
                adapter.setGridMode(false);
                rvLibrary.setLayoutManager(new LinearLayoutManager(this));
                btnGrid.setBackgroundTintList(ColorStateList.valueOf(0xFF1B2D55));
                btnList.setBackgroundTintList(ColorStateList.valueOf(0xFF667EEA));
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Filter chips
    // ──────────────────────────────────────────────

    void setupChips() {
        LinearLayout chipGroup = findViewById(R.id.chipGroupLibrary);
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
                filterAndSort();
            });

            chipGroup.addView(chip);
        }
    }

    // ──────────────────────────────────────────────
    //  Navigation
    // ──────────────────────────────────────────────

    private void setupNavigation() {
        String username = getIntent().getStringExtra("username");

        findViewById(R.id.btnLibraryUser).setOnClickListener(v -> openProfile(username));

        // Trang chủ
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            if (username != null) intent.putExtra("username", username);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Khám phá
        findViewById(R.id.navExplore).setOnClickListener(v -> {
            Intent intent = new Intent(this, ExploreActivity.class);
            if (username != null) intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> openProfile(username));
    }

    private void openProfile(String username) {
        Intent intent = new Intent(this, ProfileActivity.class);
        if (username != null) intent.putExtra("username", username);
        startActivity(intent);
        finish();
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
                filterAndSort();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(LibraryActivity.this,
                        "Không tải được dữ liệu: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Lọc theo chip + search, rồi sắp xếp rating cao → thấp.
     */
    void filterAndSort() {
        filteredBooks.clear();

        for (Book b : allBooks) {
            boolean matchType;
            switch (currentFilter) {
                case "All":      matchType = true;             break;
                case "Favorite": matchType = b.isFavorite();   break;
                case "Offline":  matchType = b.isDownloaded(); break;
                case "History":  matchType = b.getProgress() > 0; break;
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

        // ★ Sắp xếp rating cao nhất lên đầu
        filteredBooks.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));

        adapter.notifyDataSetChanged();
        updateCount();
    }

    private void updateCount() {
        TextView tvCount = findViewById(R.id.tvLibraryCount);
        tvCount.setText(filteredBooks.size() + " nội dung");
    }

    // ──────────────────────────────────────────────
    //  Book detail dialog
    // ──────────────────────────────────────────────

    private void showBookDetail(Book book) {
        String desc = (book.getDescription() == null || book.getDescription().trim().isEmpty())
                ? "Chưa có mô tả cho nội dung này."
                : book.getDescription();

        String message = desc + "\n\n"
                + "Tác giả: "    + book.getAuthor()  + "\n"
                + "Thể loại: "   + book.getGenre()   + "\n"
                + "Loại: "       + book.getType()    + "\n"
                + "Năm: "        + book.getYear()    + "\n"
                + "Đánh giá: ★ " + String.format(java.util.Locale.US, "%.1f", book.getRating()) + "\n"
                + "Tiến độ: "    + fmt(book.getProgress()) + " / " + fmt(book.getDuration()) + "\n"
                + "Yêu thích: "  + (book.isFavorite() ? "Có" : "Không");

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
                Toast.makeText(LibraryActivity.this,
                        newVal ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích",
                        Toast.LENGTH_SHORT).show();
                loadBooks();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(LibraryActivity.this,
                        "Không cập nhật được: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playContent(Book book) {
        int newProg = book.getProgress() + 60;
        if (newProg >= book.getDuration()) newProg = 0;
        final int fp = newProg;
        supabaseBookService.updateProgress(book.getId(), fp, new SupabaseBookService.UpdateCallback() {
            @Override
            public void onSuccess() {
                book.setProgress(fp);
                Toast.makeText(LibraryActivity.this,
                        "Đang phát: " + book.getTitle(), Toast.LENGTH_SHORT).show();
                loadBooks();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(LibraryActivity.this,
                        "Không lưu được tiến độ: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private boolean safeEquals(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private boolean isSupportedFilter(String key) {
        if (key == null) return false;
        for (String[] pair : FILTER_DATA) {
            if (pair[0].equals(key)) return true;
        }
        return false;
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String fmt(int seconds) {
        int m = seconds / 60, s = seconds % 60;
        return m + ":" + (s < 10 ? "0" : "") + s;
    }
}
