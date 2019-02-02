package ru.eltech.dapeshkov.speed_layer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class JSONProcessor {
    static final ObjectMapper mapper = new ObjectMapper();

    void parse(InputStream is, String file) {
        try {
            News json = mapper.readValue(is, News.class);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Item {
        private String anons;

        @Override
        public String toString() {
            return "Item{" +
                    "anons='" + anons + '\'' +
                    ", authors='" + authors + '\'' +
                    ", category='" + category + '\'' +
                    ", fronturl='" + fronturl + '\'' +
                    ", id='" + id + '\'' +
                    ", opinion_authors='" + opinion_authors + '\'' +
                    ", photo=" + photo +
                    ", project='" + project + '\'' +
                    ", publish_date='" + publish_date + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }

        private String authors;
        private String category;
        private String fronturl;
        private String id;
        private String opinion_authors;
        private Photo photo;

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }

        public Photo getPhoto() {
            return photo;
        }

        private String project;
        private String publish_date;
        private String title;

        public void setAnons(String anons) {
            this.anons = anons;
        }

        public void setAuthors(String authors) {
            this.authors = authors;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setFronturl(String fronturl) {
            this.fronturl = fronturl;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setOpinion_authors(String opinion_authors) {
            this.opinion_authors = opinion_authors;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public void setPublish_date(String publish_date) {
            this.publish_date = publish_date;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAnons() {
            return anons;
        }

        public String getAuthors() {
            return authors;
        }

        public String getCategory() {
            return category;
        }

        public String getFronturl() {
            return fronturl;
        }

        public String getId() {
            return id;
        }

        public String getOpinion_authors() {
            return opinion_authors;
        }

        public String getProject() {
            return project;
        }

        public String getPublish_date() {
            return publish_date;
        }

        public String getTitle() {
            return title;
        }
    }

    static class Photo {
        @Override
        public String toString() {
            return "Photo{" +
                    "url='" + url + '\'' +
                    '}';
        }

        private String url;

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class News {
        private Item[] items;

        public void setItems(Item[] items) {
            this.items = items;
        }

        @Override
        public String toString() {
            return "News{" +
                    "items=" + Arrays.toString(items) +
                    '}';
        }

        public Item[] getItems() {
            return items;
        }
    }
}