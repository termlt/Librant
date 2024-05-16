package com.librant.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Book implements Serializable, Parcelable {
    private String bookId;
    private String title;
    private String authorName;
    private String authorSurname;
    private String description;
    private String imageUrl;
    private String ownerId;
    private List<String> genres;
    private boolean approved;
    private String language;
    private int pageCount;
    private int ageLimit;
    private String availability;
    private Date availabilityDate;
    private List<String> borrowers;

    public Book() {}

    public Book(String bookId, String title, String authorName, String authorSurname,
                String description, String imageUrl, String ownerId, List<String> genres,
                boolean approved, String language, int pageCount, int ageLimit, String availability,
                Date availabilityDate, List<String> borrowers) {
        this.bookId = bookId;
        this.title = title;
        this.authorName = authorName;
        this.authorSurname = authorSurname;
        this.description = description;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
        this.genres = genres;
        this.approved = approved;
        this.language = language;
        this.pageCount = pageCount;
        this.ageLimit = ageLimit;
        this.availability = availability;
        this.availabilityDate = availabilityDate;
        this.borrowers = borrowers;
    }

    protected Book(Parcel in) {
        bookId = in.readString();
        title = in.readString();
        authorName = in.readString();
        authorSurname = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        ownerId = in.readString();
        genres = in.createStringArrayList();
        approved = in.readByte() != 0;
        language = in.readString();
        pageCount = in.readInt();
        ageLimit = in.readInt();
        availability = in.readString();
        availabilityDate = new Date(in.readLong());
        borrowers = in.createStringArrayList();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorSurname() {
        return authorSurname;
    }

    public void setAuthorSurname(String authorSurname) {
        this.authorSurname = authorSurname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        this.ageLimit = ageLimit;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public Date getAvailabilityDate() {
        return availabilityDate;
    }

    public void setAvailabilityDate(Date availabilityDate) {
        this.availabilityDate = availabilityDate;
    }

    public List<String> getBorrowers() {
        return borrowers;
    }

    public void setBorrowers(List<String> borrowers) {
        this.borrowers = borrowers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(title);
        dest.writeString(authorName);
        dest.writeString(authorSurname);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(ownerId);
        dest.writeStringList(genres);
        dest.writeByte((byte) (approved ? 1 : 0));
        dest.writeString(language);
        dest.writeInt(pageCount);
        dest.writeInt(ageLimit);
        dest.writeString(availability);
        dest.writeLong(availabilityDate != null ? availabilityDate.getTime() : -1);
        dest.writeStringList(borrowers);
    }
}
