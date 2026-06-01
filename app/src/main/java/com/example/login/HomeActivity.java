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
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    List<Book> allBooks = new ArrayList<>();
    List<Book> filteredBooks = new ArrayList<>();

    BookAdapter adapter;
    RecyclerView recyclerView;
    FonosDatabaseHelper dbHelper;

    String currentFilter = "All";
    String currentSearch = "";
    boolean isGrid = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getSavedLanguage(newBase);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        requestNotificationPermission();

        dbHelper = new FonosDatabaseHelper(this);

        String username = getIntent().getStringExtra("username");
        if (username != null) {
            ((TextView) findViewById(R.id.tvGreeting))
                    .setText(getString(R.string.hello_user, username));
        }

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new BookAdapter(filteredBooks, true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        adapter.setOnBookActionListener(new BookAdapter.OnBookActionListener() {
            @Override
            public void onPlay(Book book) {
                playContent(book);
            }

            @Override
            public void onFavorite(Book book) {
                boolean newValue = !book.isFavorite();
                dbHelper.updateFavorite(book.getId(), newValue);
                Toast.makeText(HomeActivity.this,
                        newValue ? getString(R.string.added_favorite) : getString(R.string.removed_favorite),
                        Toast.LENGTH_SHORT).show();
                loadBooks();
            }

            @Override
            public void onDownload(Book book) {
                boolean newValue = !book.isDownloaded();
                dbHelper.updateDownloaded(book.getId(), newValue);
                Toast.makeText(HomeActivity.this,
                        newValue ? getString(R.string.downloaded_offline) : getString(R.string.removed_offline),
                        Toast.LENGTH_SHORT).show();
                loadBooks();
            }

            @Override
            public void onEdit(Book book) {
                showBookDialog(book);
            }

            @Override
            public void onDelete(Book book) {
                confirmDelete(book);
            }
        });

        setupChips();
        setupButtons();
        setupSearch();

        loadBooks();
    }

    private void setupButtons() {
        Button btnGrid = findViewById(R.id.btnGrid);
        Button btnList = findViewById(R.id.btnList);
        TextView btnLogout = findViewById(R.id.btnLogout);
        Button btnFilter = findViewById(R.id.btnFilter);

        findViewById(R.id.fabAdd).setOnClickListener(v -> showBookDialog(null));

        btnLogout.setOnClickListener(v -> {
            stopService(new Intent(this, FonosAudioService.class));
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnFilter.setOnClickListener(v -> showFilterDialog());

        btnGrid.setOnClickListener(v -> {
            if (!isGrid) {
                isGrid = true;
                adapter.setGridMode(true);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                btnGrid.setBackgroundTintList(ColorStateList.valueOf(0xFF667EEA));
                btnList.setBackgroundTintList(ColorStateList.valueOf(0xFFE8E8E0));
            }
        });

        btnList.setOnClickListener(v -> {
            if (isGrid) {
                isGrid = false;
                adapter.setGridMode(false);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                btnGrid.setBackgroundTintList(ColorStateList.valueOf(0xFFE8E8E0));
                btnList.setBackgroundTintList(ColorStateList.valueOf(0xFF667EEA));
            }
        });
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                filterBooks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadBooks() {
        allBooks.clear();
        allBooks.addAll(dbHelper.getAllContents());
        filterBooks();
    }

    void setupChips() {
        LinearLayout chipGroup = findViewById(R.id.chipGroup);
        chipGroup.removeAllViews();

        String[] filterKeys = {
                "All",
                "Audiobook",
                "Podcast",
                "Meditation",
                "Book Summary",
                "Favorite",
                "Offline"
        };

        for (String key : filterKeys) {
            TextView chip = new TextView(this);
            chip.setText(getFilterDisplayName(key));
            chip.setTextSize(12f);
            chip.setPadding(32, 12, 32, 12);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);

            boolean isActive = key.equals(currentFilter);
            chip.setBackground(getDrawable(isActive ? R.drawable.chip_active : R.drawable.chip_inactive));
            chip.setTextColor(isActive ? 0xFFFFFFFF : 0xFF666666);

            chip.setOnClickListener(v -> {
                currentFilter = key;
                filterBooks();
                setupChips();
            });

            chipGroup.addView(chip);
        }
    }

    private String getFilterDisplayName(String key) {
        switch (key) {
            case "All":
                return getString(R.string.all);
            case "Audiobook":
                return getString(R.string.audiobook);
            case "Podcast":
                return getString(R.string.podcast);
            case "Meditation":
                return getString(R.string.meditation);
            case "Book Summary":
                return getString(R.string.book_summary);
            case "Favorite":
                return getString(R.string.favorite);
            case "Offline":
                return getString(R.string.offline);
            default:
                return key;
        }
    }

    void filterBooks() {
        filteredBooks.clear();

        for (Book b : allBooks) {
            boolean matchGenre;

            if (currentFilter.equals("All")) {
                matchGenre = true;
            } else if (currentFilter.equals("Favorite")) {
                matchGenre = b.isFavorite();
            } else if (currentFilter.equals("Offline")) {
                matchGenre = b.isDownloaded();
            } else {
                matchGenre = b.getType().equals(currentFilter);
            }

            boolean matchSearch = currentSearch.isEmpty()
                    || b.getTitle().toLowerCase().contains(currentSearch.toLowerCase())
                    || b.getAuthor().toLowerCase().contains(currentSearch.toLowerCase())
                    || b.getGenre().toLowerCase().contains(currentSearch.toLowerCase())
                    || b.getTopic().toLowerCase().contains(currentSearch.toLowerCase())
                    || b.getType().toLowerCase().contains(currentSearch.toLowerCase());

            if (matchGenre && matchSearch) {
                filteredBooks.add(b);
            }
        }

        adapter.notifyDataSetChanged();
        updateCount();
    }

    void updateCount() {
        ((TextView) findViewById(R.id.tvCount))
                .setText(getString(R.string.content_count, filteredBooks.size()));
    }

    private void playContent(Book book) {
        int newProgress = book.getProgress() + 60;

        if (newProgress > book.getDuration()) {
            newProgress = 0;
        }

        dbHelper.updateProgress(book.getId(), newProgress);

        Intent intent = new Intent(this, FonosAudioService.class);
        intent.setAction(FonosAudioService.ACTION_START);
        intent.putExtra("title", book.getTitle());
        intent.putExtra("type", book.getType());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        Toast.makeText(this,
                getString(R.string.now_playing, book.getTitle(), formatTime(newProgress)),
                Toast.LENGTH_SHORT).show();

        loadBooks();
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainSeconds = seconds % 60;
        return minutes + ":" + (remainSeconds < 10 ? "0" : "") + remainSeconds;
    }

    private void showFilterDialog() {
        String[] filterKeys = {
                "All",
                "Audiobook",
                "Podcast",
                "Meditation",
                "Book Summary",
                "Favorite",
                "Offline"
        };

        String[] displayNames = new String[filterKeys.length];

        for (int i = 0; i < filterKeys.length; i++) {
            displayNames[i] = getFilterDisplayName(filterKeys[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.filter_content))
                .setItems(displayNames, (dialog, which) -> {
                    currentFilter = filterKeys[which];
                    filterBooks();
                    setupChips();
                })
                .show();
    }

    private void showBookDialog(Book oldBook) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etTitle = new EditText(this);
        etTitle.setHint(getString(R.string.content_title));

        EditText etAuthor = new EditText(this);
        etAuthor.setHint(getString(R.string.content_author));

        EditText etGenre = new EditText(this);
        etGenre.setHint(getString(R.string.content_genre));

        EditText etYear = new EditText(this);
        etYear.setHint(getString(R.string.content_year));

        EditText etType = new EditText(this);
        etType.setHint(getString(R.string.content_type));

        EditText etTopic = new EditText(this);
        etTopic.setHint(getString(R.string.content_topic));

        EditText etDuration = new EditText(this);
        etDuration.setHint(getString(R.string.content_duration));

        layout.addView(etTitle);
        layout.addView(etAuthor);
        layout.addView(etGenre);
        layout.addView(etYear);
        layout.addView(etType);
        layout.addView(etTopic);
        layout.addView(etDuration);

        if (oldBook != null) {
            etTitle.setText(oldBook.getTitle());
            etAuthor.setText(oldBook.getAuthor());
            etGenre.setText(oldBook.getGenre());
            etYear.setText(String.valueOf(oldBook.getYear()));
            etType.setText(oldBook.getType());
            etTopic.setText(oldBook.getTopic());
            etDuration.setText(String.valueOf(oldBook.getDuration()));
        }

        new AlertDialog.Builder(this)
                .setTitle(oldBook == null ? getString(R.string.add_content) : getString(R.string.edit_content))
                .setView(layout)
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String author = etAuthor.getText().toString().trim();
                    String genre = etGenre.getText().toString().trim();
                    String type = etType.getText().toString().trim();
                    String topic = etTopic.getText().toString().trim();

                    if (title.isEmpty() || author.isEmpty() || genre.isEmpty()
                            || type.isEmpty() || topic.isEmpty()) {
                        Toast.makeText(this, getString(R.string.fill_all_info), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int year = parseNumber(etYear.getText().toString(), 2024);
                    int duration = parseNumber(etDuration.getText().toString(), 3600);

                    if (oldBook == null) {
                        Book newBook = new Book(0, title, author, genre, year,
                                type, topic, duration, 0, false, false);
                        dbHelper.addContent(newBook);
                        Toast.makeText(this, getString(R.string.added_content), Toast.LENGTH_SHORT).show();
                    } else {
                        oldBook.setTitle(title);
                        oldBook.setAuthor(author);
                        oldBook.setGenre(genre);
                        oldBook.setYear(year);
                        oldBook.setType(type);
                        oldBook.setTopic(topic);
                        oldBook.setDuration(duration);

                        dbHelper.updateContent(oldBook);
                        Toast.makeText(this, getString(R.string.updated_content), Toast.LENGTH_SHORT).show();
                    }

                    loadBooks();
                    setupChips();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private int parseNumber(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void confirmDelete(Book book) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_content))
                .setMessage(getString(R.string.delete_confirm, book.getTitle()))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    dbHelper.deleteContent(book.getId());
                    Toast.makeText(this, getString(R.string.deleted_content), Toast.LENGTH_SHORT).show();
                    loadBooks();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }
}