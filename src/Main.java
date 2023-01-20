import java.io.FileReader;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class Main {
    static JSONArray scenes;
    static JSONArray inv;
    static JSONArray used;
    static JSONArray found;
    List<List<Integer>> lists = new ArrayList<>();

    public static void main(String[] args) {
        File stories = new File("stories");
        if (!stories.exists()){
            stories.mkdirs();
        }
        File saves = new File("saves");
        if (!saves.exists()){
            saves.mkdirs();
        }

        List<String> results = new ArrayList<String>();

        File[] files = new File("stories").listFiles();

        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }

        if (results.size() == 0){
            System.out.println("Zadne nalezene pribehy.");
            System.exit(0);
        }

        System.out.println("Zadejte cislo pribehu, ktery chcete vybrat:");
        for (int i = 0; i < results.size(); i++){
            System.out.println(i + ": " + results.get(i));
        }

        Scanner sc = new Scanner(System.in);
        int story = -2;
        if (sc.hasNextLine())
            try {
                story = sc.nextInt();
            } catch (Exception e) {
                System.out.println("Zadali jste neplatne cislo pribehu.");
                System.exit(0);
            }
        sc.nextLine();
        if (story >= results.size() || story < 0){
            System.out.println("Neplatne cislo pribehu.");
            System.exit(0);
        }
        String storyName = results.get(story);

        if (!storyName.contains(".json")){
            System.out.println("Neplatny pribeh.");
            System.exit(0);
        }

        StringBuilder json = new StringBuilder();

        try {
            File myObj = new File("stories/" + storyName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {;
                json.append(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading file.");
            e.printStackTrace();
            System.exit(1);
        }

        JSONParser parser = new JSONParser();
        JSONObject mainJson = null;
        try {
            Object storyObj = parser.parse(json.toString());
            mainJson = (JSONObject) storyObj;
            if (mainJson.containsKey("story") && mainJson.containsKey("inventory") && mainJson.containsKey("id") && mainJson.containsKey("name")){
                scenes = (JSONArray) mainJson.get("story");
                System.out.println("Nacteno: " + mainJson.get("name"));
            } else {
                System.out.println("Neplatny pribeh.");
                System.exit(0);
            }
        } catch (ParseException f) {
            f.printStackTrace();
            System.out.println("An error occurred while parsing JSON exiting...");
            System.exit(1);
        }

        File save = new File("saves/" + mainJson.get("id") + ".json");
        JSONParser saveParser = new JSONParser();
        Object saveObj = null;
        try {
            if (save.exists()){
                saveObj = saveParser.parse(new FileReader("saves/" + mainJson.get("id") + ".json"));
                JSONObject saveJson = (JSONObject) saveObj;
                if (saveJson.containsKey("inventory") && saveJson.containsKey("used") && saveJson.containsKey("found") && saveJson.containsKey("roomNmb") && saveJson.containsKey("textNmb")){
                    inv = (JSONArray) saveJson.get("inventory");
                    used = (JSONArray) saveJson.get("used");
                    found = (JSONArray) saveJson.get("found");
                } else {
                    System.out.println("Neplatny save.");
                    System.exit(0);
                }
            } else {
                inv = (JSONArray) mainJson.get("inventory");
                used = new JSONArray();
                found = new JSONArray();
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while parsing JSON save file, exiting...");
            System.exit(1);
        }

    }

    static class cls {
        public cls() throws IOException {
            System.out.println("\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r");
            System.out.println("\033[H\033[2J");
        }
    }
//    public void printScene(int roomNumber, int textNumber) {
//        System.out.println(scene.get("room"));
//    }
}