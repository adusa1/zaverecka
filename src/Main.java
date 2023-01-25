import java.io.*;
import java.util.*;
import java.util.random.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Main {
    static JSONArray inv;
    static JSONArray used;
    static JSONArray found;
    static JSONObject data = null;
    static JSONArray rooms = null;
    static JSONArray items = null;
    List<List<Integer>> lists = new ArrayList<>();

    public static void main(String[] args) {
        int textload = 0, roomLoad = 0;

        File stories = new File("stories");
        if (!stories.exists()) {
            stories.mkdirs();
        }
        File saves = new File("saves");
        if (!saves.exists()) {
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

        if (results.size() == 0) {
            System.out.println("Zadne nalezene pribehy.");
            System.exit(0);
        }

        System.out.println("Zadejte cislo pribehu, ktery chcete vybrat:");
        for (int i = 0; i < results.size(); i++) {
            System.out.println(i + ": " + results.get(i));
        }

        Scanner sc = new Scanner(System.in);
        int story = -2;
        if (sc.hasNextLine()) {
            try {
                story = sc.nextInt();
            } catch (Exception e) {
                System.out.println("Zadali jste neplatne cislo pribehu.");
                System.exit(0);
            }
        }
        sc.nextLine();
        if (story >= results.size() || story < 0) {
            System.out.println("Neplatne cislo pribehu.");
            System.exit(0);
        }
        String storyName = results.get(story);

        if (!storyName.contains(".json")) {
            System.out.println("Neplatny pribeh.");
            System.exit(0);
        }

        StringBuilder json = new StringBuilder();

        try {
            File myObj = new File("stories/" + storyName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                ;
                json.append(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading file.");
            e.printStackTrace();
            System.exit(1);
        }

        JSONParser parser = new JSONParser();
        try {
            Object storyObj = parser.parse(json.toString());
            data = (JSONObject) storyObj;
            if (data.containsKey("story") && data.containsKey("inventory") && data.containsKey("id") && data.containsKey("name")) {
                rooms = (JSONArray) data.get("story");
                items = (JSONArray) data.get("inventory");
                System.out.println("Nacteno: " + data.get("name"));
            } else {
                System.out.println("Neplatny pribeh.");
                System.exit(0);
            }
        } catch (ParseException f) {
            f.printStackTrace();
            System.out.println("An error occurred while parsing JSON exiting...");
            System.exit(1);
        }

        boolean success = false;
        File save = new File("saves/" + data.get("id") + ".json");
        JSONParser saveParser = new JSONParser();
        Object saveObj = null;
        try {
            if (save.exists()) {
                saveObj = saveParser.parse(new FileReader("saves/" + data.get("id") + ".json"));
                JSONObject saveJson = (JSONObject) saveObj;
                if (saveJson.containsKey("inv") && saveJson.containsKey("used") && saveJson.containsKey("found") && saveJson.containsKey("roomNmb") && saveJson.containsKey("textNmb")) {
                    inv = (JSONArray) saveJson.get("inv");
                    used = (JSONArray) saveJson.get("used");
                    found = (JSONArray) saveJson.get("found");
                    roomLoad = ((Long) saveJson.get("roomNmb")).intValue();
                    textload = ((Long) saveJson.get("textNmb")).intValue();
                    success = true;
                } else {
                    System.out.println("Neplatny save.");
                }
            }
            if (!success) {
                inv = new JSONArray();
                used = new JSONArray();
                found = new JSONArray();

                System.out.println("Nacteni deafultnich dat dokonceno!");
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while parsing JSON save file, exiting...");
            System.exit(1);
        }
        tell(roomLoad, textload);
    }

    public static void save(int roomNmb, int textNmb, boolean reset) {
        new cls();
        System.out.println("Ukladani hry...");

        File saves = new File("saves");
        if (!saves.exists()) {
            saves.mkdirs();
        }

        if (reset) {
            inv = new JSONArray();
            used = new JSONArray();
            found = new JSONArray();
            roomNmb = 0;
            textNmb = 0;
        }

        JSONObject saveJson = new JSONObject();
        saveJson.put("inv", inv);
        saveJson.put("used", used);
        saveJson.put("found", found);
        saveJson.put("roomNmb", roomNmb);
        saveJson.put("textNmb", textNmb);
        try (FileWriter file = new FileWriter("saves/" + data.get("id") + ".json")) {
            file.write(saveJson.toJSONString());
            file.flush();

            System.out.println("Ulozeno!");
            if (reset) {
                System.out.println("Hra byla resetovana!");
                System.exit(69);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while saving game...");
            e.printStackTrace();
        }
        tell(roomNmb, textNmb, true, "Hra byla ulozena!");
    }

    public static void inventory(int roomNmb, int textNmb) {
        new cls();
        System.out.println("Polozky v inventari:");

        if (inv.size() == 0) {
            System.out.println("V inventari nic neni.");
        } else {
            for (Object o : inv) {
                int id = ((Long) o).intValue();
                JSONObject item = (JSONObject) items.get(id);
                System.out.println(item.get("name") + ": " + item.get("text"));
            }
        }
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        tell(roomNmb, textNmb, true, "");
    }

    public static void chance() {
        System.out.println("HELP!!!");
    }

    public static void tell(int roomNmb, int textNmb) {
        tell(roomNmb, textNmb, false, "");
    }

    public static void tell(int roomNmb, int textNmb, boolean skip, String message) {
        JSONObject room = (JSONObject) rooms.get(roomNmb);
        JSONArray texts = (JSONArray) room.get("texts");
        JSONObject text = (JSONObject) texts.get(textNmb);
        JSONArray answers = (JSONArray) room.get("answers");

        new cls();

        chance();

        System.out.println("Nachazis se v: " + room.get("name"));

        // letter by letter printing of text
        if (!skip) {
            String textToPrint = (String) text.get("text");
            char[] longWait = {'.', ',', '?', '!', ';'};
            for (int i = 0; i < textToPrint.length(); i++) {
                char c = textToPrint.charAt(i);
                System.out.print(c);
                System.out.flush();
                try {
                    if (new String(longWait).contains(String.valueOf(c))) {
                        Thread.sleep(300);
                    } else {
                        Thread.sleep(40);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
        } else {
            System.out.println(text.get("text"));
        }
        System.out.println();
        List<Integer> answery = new ArrayList<>();
        answery.add(-1);
        answery.add(-2);
        answery.add(-3);

        Hashtable<Integer, Integer> link = new Hashtable<Integer, Integer>();
        int deductable = 0;

        JSONArray tanswers = (JSONArray) text.get("answers");

        for (int ine = 0; ine < tanswers.size(); ine++) {
            int answerNmb = ((Long) tanswers.get(ine)).intValue();
            int iny = ine - deductable;
            JSONObject answer = (JSONObject) answers.get(answerNmb);
            String toCompare = roomNmb + "-" + answerNmb + "-";

            if (!(boolean) answer.get("repeatable")) {
                boolean skipy = false;
                for (Object o : used) {
                    StringBuilder sb = new StringBuilder();
                    JSONArray foundArray = (JSONArray) o;
                    for (Object o1 : foundArray) {
                        sb.append(o1.toString());
                        sb.append("-");
                    }
                    if (sb.toString().equals(toCompare)) {
                        skipy = true;
                        break;
                    }
                }
                if (skipy) {
                    deductable++;
                    continue;
                }
            }

            if ((boolean) answer.get("hidden")) {
                boolean skipy = true;
                for (Object o : found) {
                    StringBuilder sb = new StringBuilder();
                    JSONArray foundArray = (JSONArray) o;
                    for (Object o1 : foundArray) {
                        sb.append(o1.toString());
                        sb.append("-");
                    }
                    if (sb.toString().equals(toCompare)) {
                        skipy = false;
                        break;
                    }
                }
                if (skipy) {
                    deductable++;
                    continue;
                }
            }

            JSONArray required = (JSONArray) answer.get("requires");
            Set<Integer> s = new HashSet<Integer>();
            for (Object j : inv) {
                int item = ((Long) j).intValue();
                s.add(item);
            }
            int p = s.size();
            for (Object j : required) {
                int item = ((Long) j).intValue();
                s.add(item);
            }

            if (s.size() == p) {
                System.out.println(iny + ": " + answer.get("text"));
                link.put(iny, answerNmb);
                answery.add(iny);
                continue;
            }

            ArrayList<String> itemy = new ArrayList<>();
            ArrayList<String> itemyMissing = new ArrayList<>();

            for (Object itemNmbTmp : (JSONArray) answer.get("requires")) {
                int itemNmb = ((Long) itemNmbTmp).intValue();
                itemy.add((String) ((JSONObject) items.get(itemNmb)).get("name"));
                if (!inv.contains((long) itemNmb)) {
                    itemyMissing.add((String) ((JSONObject) items.get(itemNmb)).get("name"));
                }
            }
            String requires = String.join(" a ", itemy);
            String missing = String.join(", ", itemyMissing);
            System.out.println("*: " + answer.get("text") + " (Vyzaduje " + requires + ", chybi " + missing + ")");
        }

        System.out.println("\n----------------\n-1: Ulozit\n-2: Ukoncit\n-3: Inventar\n----------------\n\n");
        System.out.println(message.equals("") ? "" : message);

        int answerNmb;
        while (true) {
            Scanner sc = new Scanner(System.in);
            try {
                answerNmb = sc.nextInt();
                if (!answery.contains(answerNmb)) {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("\033[A\033[A");
                System.out.println("\033[A\033[A");
                System.out.println("Neplatne cislo odpovedi!");
                continue;
            }
            break;
        }

        switch (answerNmb) {
            case -1 -> save(roomNmb, textNmb, false);
            case -2 -> System.exit(5318008);
            case -3 -> inventory(roomNmb, textNmb);
            default -> {
                JSONObject answer = (JSONObject) answers.get(link.get(answerNmb));
                for (Object itemNmbTmp : (JSONArray) answer.get("gives")) {
                    int itemNmb = ((Long) itemNmbTmp).intValue();
                    if (!inv.contains((long) itemNmb)) {
                        inv.add((long) itemNmb);
                    }
                }
                for (Object itemNmbTmp : (JSONArray) answer.get("takes")) {
                    int itemNmb = ((Long) itemNmbTmp).intValue();
                    if (inv.contains((long) itemNmb)) {
                        inv.remove((long) itemNmb);
                    }
                }

                for (Object itemTmp : (JSONArray) answer.get("unlocks")) {
                    JSONObject item = (JSONObject) itemTmp;
                    String toCompare = item.get("room") + "-" + item.get("unlock") + "-";
                    boolean add = true;
                    for (Object o : found) {
                        JSONArray foundArray = (JSONArray) o;
                        StringBuilder sb = new StringBuilder();
                        for (Object o1 : foundArray) {
                            sb.append(o1.toString());
                            sb.append("-");
                        }
                        if (sb.toString().equals(toCompare)) {
                            add = false;
                        }
                    }
                    if (add) {
                        JSONArray foundArray = new JSONArray();
                        foundArray.add(item.get("room"));
                        foundArray.add(item.get("unlock"));
                        found.add(foundArray);
                    }
                }

                if (!(boolean) answer.get("repeatable")) {
                    JSONArray usedArray = new JSONArray();
                    usedArray.add(roomNmb);
                    usedArray.add(link.get(answerNmb));
                    used.add(usedArray);
                }

                if (((Long) answer.get("goto")).intValue() == -1) {
                    Random rn =new Random();
            
                    int cislo= rn.nextInt(5-0+1);
                    switch (cislo) {
                        case 0:
                            System.out.println("Jsi dead");
                            break;
                    
                        case 1:
                            System.out.println("Snaz se vic");
                            break;
                       
                        case 2:
                            System.out.println("Game over");
                            break;

                        case 3:
                            System.out.println("Takze od znova");
                            break;

                        case 4:
                            System.out.println("Umrel jsi");
                            break;

                        case 5:
                            System.out.println("Treba priste");
                

                        default:
                            break;
                    }

                    save(0, 0, true);
                    System.exit(69);
                }

                tell(((Long) answer.get("goto")).intValue(), ((Long) answer.get("tell")).intValue());
            }
        }
    }
    public static synchronized void playDong() {
        try {
            File f = new File("dependencies/DialougeSound.wav");
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(f.toURI().toURL());
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    public static class cls {
        public cls() {
            System.out.println("\n\r".repeat(100));
            System.out.println("\033[H\033[2J");
        }
    }
}
