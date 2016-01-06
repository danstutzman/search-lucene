package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.json.JSONArray;
import org.json.JSONObject;

public class LuceneExample {
  public static void main(String[] args) throws CorruptIndexException,
      LockObtainFailedException, IOException {
    if (args.length < 1) {
       System.err.println("First argument: location of .json file");
       System.exit(1);
    }
    File jsonFile = new File(args[0]);

    if (args.length < 2) {
       System.err.println("Second argument: directory to build Lucene index in");
       System.exit(1);
    }
    File indexFile = new File(args[1]);

    Directory indexDirectory = FSDirectory.open(indexFile.toPath());
    //StandardAnalyzer analyzer = new StandardAnalyzer();
    SpanishAnalyzer analyzer = new SpanishAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(OpenMode.CREATE);
    IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

    BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
    String line;
    Set<String> existingArtistAndSongNames = new HashSet<String>();
    while ((line = reader.readLine()) != null) {
      JSONObject object = new JSONObject(line);
      if (object.getString("type").equals("song_text")) {
        String artistName = object.getString("artist_name");
        String songName = object.getString("song_name");

        String artistAndSongName = artistName + " - " + songName;
        if (!existingArtistAndSongNames.contains(artistAndSongName)) {
          existingArtistAndSongNames.add(artistAndSongName);

          JSONArray songTextLines = object.getJSONArray("song_text");
          StringBuilder songText = new StringBuilder();
          for (int i = 0; i < songTextLines.length(); i++) {
            String songTextLine = songTextLines.getString(i);
            songText.append(songTextLine.trim());
            songText.append("\n");
          }

          Document doc = new Document();
          doc.add(new TextField("artist_name", artistName, Field.Store.YES));
          doc.add(new TextField("song_name", songName, Field.Store.YES));
          doc.add(new TextField("song_text", songText.toString(), Field.Store.YES));
          indexWriter.addDocument(doc);
        }
      }
    }
    indexWriter.close();
  }
}
