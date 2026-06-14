import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


//  PLAYER
class Player implements Comparable<Player> {

    private String name;
    private int wins;
    private int losses;
    private int points;

    public Player(String name) {
        this.name = name;
        this.wins = 0;
        this.losses = 0;
        this.points = 0;
    }

    public void addWin() {
        wins++;
        points += 3;
    }

    public void addLoss() {
        losses++;
    }

    public String getName()   { return name; }
    public int getWins()      { return wins; }
    public int getLosses()    { return losses; }
    public int getPoints()    { return points; }

    @Override
    public int compareTo(Player other) {
        return Integer.compare(other.points, this.points);
    }

    @Override
    public String toString() {
        return String.format("%-20s | Wins: %2d | Losses: %2d | Points: %3d",
                name, wins, losses, points);
    }
}

// ─────────────────────────────────────────────
//  LEADERBOARD HEAP  (Max Heap by points)
// ─────────────────────────────────────────────
class LeaderboardHeap {

    private Player[] heap;
    private int size;
    private int capacity;

    public LeaderboardHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new Player[capacity];
        this.size = 0;
    }

    private int parent(int i)    { return (i - 1) / 2; }
    private int leftChild(int i) { return 2 * i + 1; }
    private int rightChild(int i){ return 2 * i + 2; }

    private void swap(int i, int j) {
        Player temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public void insert(Player p) {
        if (size == capacity) {
            System.out.println("Leaderboard is full.");
            return;
        }
        heap[size] = p;
        int i = size;
        size++;
        while (i > 0 && heap[i].compareTo(heap[parent(i)]) < 0) {
            swap(i, parent(i));
            i = parent(i);
        }
    }

    private void heapifyDown(int i) {
        int largest = i;
        int left    = leftChild(i);
        int right   = rightChild(i);
        if (left  < size && heap[left].compareTo(heap[largest])  < 0) largest = left;
        if (right < size && heap[right].compareTo(heap[largest]) < 0) largest = right;
        if (largest != i) {
            swap(i, largest);
            heapifyDown(largest);
        }
    }

    public void rebuildHeap() {
        for (int i = size / 2 - 1; i >= 0; i--) {
            heapifyDown(i);
        }
    }

    public void printLeaderboard() {
        if (size == 0) {
            System.out.println("  No players in leaderboard.");
            return;
        }
        Player[] sorted = new Player[size];
        for (int i = 0; i < size; i++) sorted[i] = heap[i];

        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = 0; j < sorted.length - i - 1; j++) {
                if (sorted[j].compareTo(sorted[j + 1]) > 0) {
                    Player tmp = sorted[j];
                    sorted[j] = sorted[j + 1];
                    sorted[j + 1] = tmp;
                }
            }
        }
        System.out.println("  Rank | Player               | Wins | Losses | Points");
        System.out.println("  -----|----------------------|------|--------|-------");
        for (int i = 0; i < sorted.length; i++) {
            System.out.printf("  %-4d | %s%n", i + 1, sorted[i].toString());
        }
    }
}

// ─────────────────────────────────────────────
//  MATCH NODE  (Linked List node)
// ─────────────────────────────────────────────
class MatchNode {

    String player1;
    String player2;
    String winner;
    int round;
    MatchNode next;

    public MatchNode(String player1, String player2, String winner, int round) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner  = winner;
        this.round   = round;
        this.next    = null;
    }

    @Override
    public String toString() {
        return String.format("Round %d: %s vs %s  -->  Winner: %s",
                round, player1, player2, winner);
    }
}

// ─────────────────────────────────────────────
//  MATCH HISTORY  (Singly Linked List)
// ─────────────────────────────────────────────
class MatchHistory {

    private MatchNode head;
    private int size;

    public MatchHistory() {
        head = null;
        size = 0;
    }

    public void addMatch(String p1, String p2, String winner, int round) {
        MatchNode node = new MatchNode(p1, p2, winner, round);
        if (head == null) {
            head = node;
        } else {
            MatchNode cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = node;
        }
        size++;
    }

    public void printAll() {
        if (head == null) {
            System.out.println("  No matches played yet.");
            return;
        }
        MatchNode cur = head;
        while (cur != null) {
            System.out.println("  " + cur.toString());
            cur = cur.next;
        }
    }

    public void printByPlayer(String name) {
        MatchNode cur = head;
        boolean found = false;
        while (cur != null) {
            if (cur.player1.equalsIgnoreCase(name) || cur.player2.equalsIgnoreCase(name)) {
                System.out.println("  " + cur.toString());
                found = true;
            }
            cur = cur.next;
        }
        if (!found) System.out.println("  No matches found for: " + name);
    }

    public int getSize() { return size; }
}

// ─────────────────────────────────────────────
//  BRACKET NODE  (Graph node)
// ─────────────────────────────────────────────
class BracketNode {

    int matchId;
    String player1;
    String player2;
    String winner;
    int round;
    BracketNode nextMatchWinner;
    BracketNode nextMatchLoser;

    public BracketNode(int matchId, String player1, String player2, int round) {
        this.matchId = matchId;
        this.player1 = player1;
        this.player2 = player2;
        this.winner  = null;
        this.round   = round;
        this.nextMatchWinner = null;
        this.nextMatchLoser  = null;
    }

    public boolean isPlayed() { return winner != null; }

    @Override
    public String toString() {
        String status = isPlayed() ? "Winner: " + winner : "Pending";
        return String.format("[Match %d | Round %d] %s  vs  %s  (%s)",
                matchId, round, player1, player2, status);
    }
}

// ─────────────────────────────────────────────
//  TOURNAMENT BRACKET  (Graph + BFS)
// ─────────────────────────────────────────────
class TournamentBracket {

    private BracketNode[] matches;
    private int matchCounter;
    private HashMap<Integer, BracketNode> matchMap;

    public TournamentBracket(int capacity) {
        this.matches      = new BracketNode[capacity];
        this.matchCounter = 1;
        this.matchMap     = new HashMap<>();
    }

    public int addMatch(String p1, String p2, int round) {
        BracketNode node = new BracketNode(matchCounter, p1, p2, round);
        matches[matchCounter] = node;
        matchMap.put(matchCounter, node);
        matchCounter++;
        return matchCounter - 1;
    }

    public boolean recordResult(int matchId, String winner) {
        BracketNode node = matchMap.get(matchId);
        if (node == null) {
            System.out.println("  Match not found.");
            return false;
        }
        if (!winner.equalsIgnoreCase(node.player1) && !winner.equalsIgnoreCase(node.player2)) {
            System.out.println("  Winner must be one of the two players in this match.");
            return false;
        }
        node.winner = winner;
        return true;
    }

    public void printBracket() {
        if (matchCounter == 1) {
            System.out.println("  No matches scheduled yet.");
            return;
        }
        int lastRound = -1;
        for (int i = 1; i < matchCounter; i++) {
            BracketNode node = matches[i];
            if (node.round != lastRound) {
                System.out.println("\n  === Round " + node.round + " ===");
                lastRound = node.round;
            }
            System.out.println("    " + node.toString());
        }
        System.out.println();
    }

    public void printBracketBFS() {
        if (matchCounter == 1) {
            System.out.println("  Bracket is empty.");
            return;
        }
        System.out.println("  BFS Bracket Traversal (Round by Round):");
        System.out.println("  ----------------------------------------");

        Queue<BracketNode> queue = new LinkedList<>();
        for (int i = 1; i < matchCounter; i++) {
            if (matches[i].round == 1) queue.add(matches[i]);
        }

        int visitedRound = -1;
        while (!queue.isEmpty()) {
            BracketNode cur = queue.poll();
            if (cur.round != visitedRound) {
                System.out.println("\n  [Round " + cur.round + "]");
                visitedRound = cur.round;
            }
            System.out.println("    " + cur.toString());
            if (cur.nextMatchWinner != null) queue.add(cur.nextMatchWinner);
        }
        System.out.println();
    }

    public BracketNode getMatch(int matchId) { return matchMap.get(matchId); }
    public int getTotalMatches()             { return matchCounter - 1; }
}

// ─────────────────────────────────────────────
//  MATCH QUEUE  (Circular Array Queue)
// ─────────────────────────────────────────────
class MatchQueue {

    private int[] matchIds;
    private int front;
    private int back;
    private int size;
    private int capacity;

    public MatchQueue(int capacity) {
        this.capacity = capacity;
        this.matchIds = new int[capacity];
        this.front    = 0;
        this.back     = -1;
        this.size     = 0;
    }

    public boolean enqueue(int matchId) {
        if (size == capacity) {
            System.out.println("Match queue is full.");
            return false;
        }
        back = (back + 1) % capacity;
        matchIds[back] = matchId;
        size++;
        return true;
    }

    public int dequeue() {
        if (isEmpty()) return -1;
        int id = matchIds[front];
        front = (front + 1) % capacity;
        size--;
        return id;
    }

    public boolean isEmpty() { return size == 0; }
    public int getSize()     { return size; }

    public void printQueue(TournamentBracket bracket) {
        if (isEmpty()) {
            System.out.println("  No upcoming matches in queue.");
            return;
        }
        System.out.println("  Upcoming Matches (Queue Order):");
        int i = front, count = 0;
        while (count < size) {
            BracketNode node = bracket.getMatch(matchIds[i % capacity]);
            if (node != null) System.out.println("    " + node.toString());
            i++;
            count++;
        }
    }
}

// ─────────────────────────────────────────────
//  MAIN
// ─────────────────────────────────────────────
public class Main {

    static HashMap<String, Player> players = new HashMap<>();
    static LeaderboardHeap leaderboard     = new LeaderboardHeap(100);
    static TournamentBracket bracket       = new TournamentBracket(200);
    static MatchQueue matchQueue           = new MatchQueue(100);
    static MatchHistory matchHistory       = new MatchHistory();
    static Scanner sc                      = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("   LEADERBOARD & TOURNAMENT BRACKET SYSTEM  ");
        System.out.println("=============================================");

        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = readInt();
            switch (choice) {
                case 1: playerMenu();      break;
                case 2: leaderboardMenu(); break;
                case 3: tournamentMenu();  break;
                case 4: historyMenu();     break;
                case 5:
                    System.out.println("\nExiting... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    static void showMainMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Player Management");
        System.out.println("2. Leaderboard");
        System.out.println("3. Tournament Bracket");
        System.out.println("4. Match History");
        System.out.println("5. Exit");
        System.out.print("Choose: ");
    }

    static void playerMenu() {
        System.out.println("\n--- PLAYER MANAGEMENT ---");
        System.out.println("1. Register Player");
        System.out.println("2. View All Players");
        System.out.println("3. Back");
        System.out.print("Choose: ");
        int ch = readInt();

        if (ch == 1) {
            System.out.print("Enter player name: ");
            String name = sc.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return;
            }
            if (players.containsKey(name.toLowerCase())) {
                System.out.println("Player already registered.");
                return;
            }
            Player p = new Player(name);
            players.put(name.toLowerCase(), p);
            leaderboard.insert(p);
            System.out.println("Player registered: " + name);

        } else if (ch == 2) {
            if (players.isEmpty()) {
                System.out.println("No players registered yet.");
                return;
            }
            System.out.println("\n  Registered Players:");
            for (Player p : players.values()) {
                System.out.println("  - " + p.getName());
            }
        }
    }

    static void leaderboardMenu() {
        System.out.println("\n--- LEADERBOARD ---");
        System.out.println("1. View Rankings");
        System.out.println("2. Record Win/Loss (Manual)");
        System.out.println("3. Search Player Stats");
        System.out.println("4. Back");
        System.out.print("Choose: ");
        int ch = readInt();

        if (ch == 1) {
            System.out.println("\n  Current Rankings:");
            leaderboard.rebuildHeap();
            leaderboard.printLeaderboard();

        } else if (ch == 2) {
            System.out.print("  Enter winner name: ");
            String winnerName = sc.nextLine().trim();
            System.out.print("  Enter loser name: ");
            String loserName = sc.nextLine().trim();

            Player winner = players.get(winnerName.toLowerCase());
            Player loser  = players.get(loserName.toLowerCase());

            if (winner == null || loser == null) {
                System.out.println("  One or both players not found.");
                return;
            }
            winner.addWin();
            loser.addLoss();
            leaderboard.rebuildHeap();
            matchHistory.addMatch(winner.getName(), loser.getName(), winner.getName(), 0);
            System.out.println("  Score updated. " + winner.getName() + " wins!");

        } else if (ch == 3) {
            System.out.print("  Enter player name: ");
            String name = sc.nextLine().trim();
            Player p = players.get(name.toLowerCase());
            if (p == null) {
                System.out.println("  Player not found.");
                return;
            }
            System.out.println("\n  " + p.toString());
        }
    }

    static void tournamentMenu() {
        System.out.println("\n--- TOURNAMENT BRACKET ---");
        System.out.println("1. Schedule a Match");
        System.out.println("2. View Full Bracket");
        System.out.println("3. View Bracket (BFS Traversal)");
        System.out.println("4. Play Next Match from Queue");
        System.out.println("5. View Upcoming Matches Queue");
        System.out.println("6. Back");
        System.out.print("Choose: ");
        int ch = readInt();

        if (ch == 1) {
            System.out.print("  Player 1 name: ");
            String p1 = sc.nextLine().trim();
            System.out.print("  Player 2 name: ");
            String p2 = sc.nextLine().trim();
            System.out.print("  Round number: ");
            int round = readInt();

            if (!players.containsKey(p1.toLowerCase()) || !players.containsKey(p2.toLowerCase())) {
                System.out.println("  Both players must be registered first.");
                return;
            }
            Player player1 = players.get(p1.toLowerCase());
            Player player2 = players.get(p2.toLowerCase());

            int matchId = bracket.addMatch(player1.getName(), player2.getName(), round);
            matchQueue.enqueue(matchId);
            System.out.println("  Match scheduled! Match ID: " + matchId);

        } else if (ch == 2) {
            bracket.printBracket();

        } else if (ch == 3) {
            bracket.printBracketBFS();

        } else if (ch == 4) {
            if (matchQueue.isEmpty()) {
                System.out.println("  No matches in queue.");
                return;
            }
            int matchId = matchQueue.dequeue();
            BracketNode node = bracket.getMatch(matchId);
            if (node == null) {
                System.out.println("  Match not found.");
                return;
            }
            System.out.println("\n  Next Match to Play:");
            System.out.println("  " + node.toString());
            System.out.print("  Enter winner name: ");
            String winnerName = sc.nextLine().trim();

            boolean ok = bracket.recordResult(matchId, winnerName);
            if (ok) {
                Player winner = players.get(winnerName.toLowerCase());
                String loserName = node.player1.equalsIgnoreCase(winnerName)
                        ? node.player2 : node.player1;
                Player loser = players.get(loserName.toLowerCase());

                if (winner != null) winner.addWin();
                if (loser  != null) loser.addLoss();

                leaderboard.rebuildHeap();
                matchHistory.addMatch(node.player1, node.player2, winnerName, node.round);
                System.out.println("  Result recorded. Winner: " + winnerName);
            }

        } else if (ch == 5) {
            matchQueue.printQueue(bracket);
        }
    }

    static void historyMenu() {
        System.out.println("\n--- MATCH HISTORY ---");
        System.out.println("1. View All Matches");
        System.out.println("2. Search by Player Name");
        System.out.println("3. Back");
        System.out.print("Choose: ");
        int ch = readInt();

        if (ch == 1) {
            System.out.println("\n  All Matches Played (" + matchHistory.getSize() + " total):");
            matchHistory.printAll();

        } else if (ch == 2) {
            System.out.print("  Enter player name: ");
            String name = sc.nextLine().trim();
            System.out.println("\n  Matches for " + name + ":");
            matchHistory.printByPlayer(name);
        }
    }

    static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}