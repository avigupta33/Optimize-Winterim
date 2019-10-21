import java.io.*;
import java.util.*;

public class GuptaOptimize {

    public int numPreferences = 8;

    public HashMap<String, Person> allPeople = new HashMap<>();
    public HashMap<String, Group> allGroups = new HashMap<>();
    ArrayList<Person> people = new ArrayList();
    ArrayList<Person> leaders = new ArrayList();


    public class Group { //an individual Winterim
        private String name;
        private int min;
        private int max;


        public Group(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public String getName() {return name;}
    }

    public class Roster {
        public Group group;
        public ArrayList<Person> participants, leaders;

        public Roster(Group g) {
            group = g;
            participants = new ArrayList<>();
            leaders = new ArrayList<>();
        }

        public Roster(Roster r) {
            this.group = r.group;
            this.participants = new ArrayList<>(r.participants);
            this.leaders = new ArrayList<>(r.leaders);
        }


        public void addLeader(Person l) {leaders.add(l);}

        public void addParticipant(Person p) {participants.add(p);}
        public void removeParticipant(Person p) {participants.remove(p);}


        public int evaluate() {
            int val = 0;
            for (Person p: participants) {
                val+=p.evaluate(group);
            }

            if (participants.size()<group.min) {
                val+= 1000*(group.min-participants.size())*(group.min-participants.size())*(group.min-participants.size())*(group.min-participants.size());
            }
            else if (participants.size()>group.max) {
                val+=10000000*group.max-participants.size();
            }
            return val;
        }

        public void display() {
            System.out.println(group.name);
            for (Person l: leaders) System.out.println(l.name + " (Leader)");
            for (Person p: participants) System.out.println(p.name);
            System.out.println();
        }
    }

    public class Person {
        private String name;
        private int grade;
        private ArrayList<Group> preferences;

        public Person(String name, int grade) {
            this.name = name;
            this.grade = grade;
            preferences = new ArrayList<>();
        }

        public int evaluate(Group chosen) {
            for (int i = 0; i<preferences.size(); i++) {
                if (preferences.get(i).getName().equals(chosen.getName())) {
                    if (i<4) {
                        return i*i*i*i;

                    }
                    else {
                        return 30*i*i*i*i;
                    }
                }
            }
          return 1000000;
        }

        public void addPreference(Group g) {preferences.add(g);}
    }

    public class Solution {
        private ArrayList<Roster> rosters = new ArrayList<>();
        private int latestEvalValue;

        public Solution() {
            latestEvalValue = 0;
        }

        public Solution(Solution s) {
            this.rosters = new ArrayList<>();
            for (Roster r: s.rosters) {
                rosters.add(new Roster(r));
            }

            this.latestEvalValue = s.latestEvalValue;
        }

        public void addRoster(Roster r) {
            rosters.add(r);
        }

        public int evaluate() {
            int e = 0;
            for (Roster r : rosters ) {
                e += r.evaluate();
            }
            latestEvalValue = e;
            return e;
        }

        public void displayRosters() {
            for (Roster r: rosters) r.display();
        }

        public void printStats() {

            int[] rankings = new int[numPreferences+1];
            for (Roster r: rosters) {
                for (Person p: r.participants) {
                    boolean broken = false;
                    for (int i = 0; i<numPreferences; i++) {
                        Group target = p.preferences.get(i);
                        if (target.equals(r.group)) {
                            rankings[i]++;
                            broken = true;
                            break;
                        }

                    }
                    if (!broken) {
                        rankings[numPreferences]++;
                    }
                }
                rankings[0]+=r.leaders.size();
            }

            int totalPeople = 0;
            int totalChoiceValue = 0;
            for (int i = 0; i<rankings.length; i++) {
                totalChoiceValue += rankings[i]*(i+1);
                totalPeople += rankings[i];
                //System.out.println("Choice Number " + (i+1) + ": " + rankings[i] + " people");
            }
            //System.out.println("Total People: " + totalPeople);
            System.out.println("Latest Eval Value: " + this.evaluate());
            System.out.println("Average Choice Value: " + (double)(totalChoiceValue)/totalPeople + "\n");

        }

        public void printCapacityStats() {
            int total = 0;
            for (Roster r: rosters) {
                System.out.println("Group: " + r.group.name + "  Min: " + r.group.min + "  Max: " + r.group.max + "  Current: " + (r.participants.size()+r.leaders.size()));
                total += (r.participants.size()+r.leaders.size());

            }
            System.out.println("Total: " + total);
        }

        public ArrayList<Swap> generateSwaps() {
            ArrayList<Swap> swaps = new ArrayList<>();
            for (int i = 0; i < rosters.size(); i++) {
                for (int j = i+1; j < rosters.size(); j++) {
                    Roster roster1 = this.rosters.get(i);
                    Roster roster2 = rosters.get(j);

                    for (Person person1 : roster1.participants) {
                        swaps.add(new Swap(roster1.group, roster2.group, person1));
                        for (Person person2 : roster2.participants) {
                            swaps.add(new Swap(roster2.group, roster1.group, person2));
                            swaps.add(new Swap(roster1.group, roster2.group, person1, person2));
                        }
                    }
                }
            }
            return swaps;
        }
    }

    public void readFiles(String person_file, String group_file, String preference_file, String leader_file) throws IOException {

        FileReader per = new FileReader(person_file);
        BufferedReader person = new BufferedReader(per);

        person.readLine();

        String currentLine = person.readLine();

        while (currentLine != null) {
            currentLine = currentLine.trim();
            String[] personParts = currentLine.split(",");
            Person p = new Person(personParts[0], Integer.parseInt(personParts[1]));
            allPeople.put(personParts[0],p);
            people.add(p);
            currentLine = person.readLine();
        }

        FileReader gro = new FileReader(group_file);
        BufferedReader group = new BufferedReader(gro);

        group.readLine();

        currentLine = group.readLine();

        while (currentLine != null) {
            currentLine = currentLine.trim();
            String[] groupParts = currentLine.split(",");
            allGroups.put(groupParts[0],new Group(groupParts[0], Integer.parseInt(groupParts[1]),Integer.parseInt(groupParts[2])));
            currentLine = group.readLine();
        }

        FileReader lead = new FileReader(leader_file);
        BufferedReader leader = new BufferedReader(lead);
        leader.readLine();

        currentLine = leader.readLine();

        while (currentLine!= null) {
            currentLine = currentLine.trim();
            String[] leaderSet = currentLine.split(",");
            String name = leaderSet[0];
            Person real_person = allPeople.get(name); //finds the actual Person object associated with this leader

            people.remove(real_person); //removes them from people
            leaders.add(real_person); //adds them to leaders

            String target_g = leaderSet[1]; //finding the group this leader leads
            Group real_group = allGroups.get(target_g);

            Group[] filler = new Group[numPreferences];
            //System.out.println("numPreferences: " + numPreferences);
            Arrays.fill(filler, real_group);
            real_person.preferences = new ArrayList<>(Arrays.asList(filler));
            //System.out.println("Leader preference size: " + real_person.preferences.size());
            currentLine = leader.readLine();
        }

        FileReader pref = new FileReader(preference_file);
        BufferedReader preference = new BufferedReader(pref);
        preference.readLine();

        currentLine = preference.readLine();

        while (currentLine!= null) {
            currentLine = currentLine.trim();
            String[] preferenceSet = currentLine.split(",");
            //numPreferences = preferenceSet.length - 1;
            String person_name = preferenceSet[0];
            Person target = allPeople.get(person_name);
            for (int i = 1; i<preferenceSet.length; i++) {
                target.addPreference(allGroups.get(preferenceSet[i]));
            }
            currentLine = preference.readLine();
        }
    }

    public Solution generateRandom() {
        Solution s = new Solution();
        for (Group g:allGroups.values()) s.addRoster(new Roster(g));
        Collections.shuffle(people);
        //System.out.println("generateRandom point 1: " + people.size() + " people");

        int lCounter = 0;
        for (Person l: leaders) {
            for (Roster r : s.rosters) {
                if (r.group == l.preferences.get(0)) {
                    r.addLeader(l);
                    lCounter++;
                }
            }
        }
        //System.out.println("generateRandom point 2: " + lCounter + " leaders added");

        int pCounter = 0;
        for (Person p: people) {
            boolean assigned = false;
            for (Group target: p.preferences) {
                for (Roster r: s.rosters) {
                    if (r.group == target) {
                        if (r.participants.size() < r.group.max) {
                            r.addParticipant(p);
                            pCounter++;
                            assigned = true;
                            break;
                        }
                    }
                }

                if (assigned) break;
            }
            if (!assigned) {
                for (Roster rand: s.rosters) {
                    if (rand.participants.size()< rand.group.max) {
                        rand.addParticipant(p);
                        pCounter++;
                        break;
                    }
                }
            }
        }
        //System.out.println("generateRandom point 3: " + pCounter + " people added");
        return s;
    }

    public Solution randomSampling(int runs) {
        Solution bestSol = generateRandom();
        int bestVal = bestSol.evaluate();
        for (int i = 0;i<runs; i++) {
            Solution newSol = generateRandom();
            int newVal = newSol.evaluate();
            if (newVal<bestVal) {
                bestSol = newSol;
                bestVal = newVal;
            }
        }
        return bestSol;
    }

    //credit to Hansen for the initial idea behind this function. All code is my own.
    public class Swap {
        private Person person1, person2;
        private Group previous, destination;
        private int swapNumber; //1 = 1 person move, 2 = 2 people switch

        //Swap of one person
        public Swap(Group previous, Group destination, Person person1) {
            this.previous = previous;
            this.destination = destination;
            this.person1 = person1;
            swapNumber = 1;
        }

        //Swap of two people
        public Swap(Group previous, Group destination, Person person1, Person person2) {
            this.previous = previous;
            this.destination = destination;
            this.person1 = person1;
            this.person2 = person2;
            swapNumber = 2;
        }

        public void execute(Solution s) {
            if (swapNumber == 1) {
                Roster roster1 = null;
                Roster roster2 = null;
                for (Roster r: s.rosters) {
                    if (r.group == previous) {
                        roster1 = r;
                    }

                    else if (r.group == destination) {
                        roster2 = r;
                    }
                }
                if (roster1!=null && roster2!=null) {
                    roster2.addParticipant(person1);
                    roster1.removeParticipant(person1);
                }
            }
            else if (swapNumber==2) {
                Roster roster1 = null;
                Roster roster2 = null;
                for (Roster r: s.rosters) {
                    if (r.group == previous) {
                        roster1 = r;
                    }

                    else if (r.group == destination) {
                        roster2 = r;
                    }
                }
                if (roster1!=null && roster2!=null) {
                    roster2.addParticipant(person1);
                    roster1.removeParticipant(person1);
                    roster1.addParticipant(person2);
                    roster2.removeParticipant(person2);
                }
            }
            //System.out.println("Executed");
        }

        public void undo(Solution s) {
            if (swapNumber == 1) {
                Roster roster1 = null;
                Roster roster2 = null;
                for (Roster r: s.rosters) {
                    if (r.group == previous) {
                        roster1 = r;
                    }

                    else if (r.group == destination) {
                        roster2 = r;
                    }
                }
                if (roster1!=null && roster2!=null) {
                    roster1.addParticipant(person1);
                    roster2.removeParticipant(person1);
                }
            }
            else if (swapNumber==2) {
                Roster roster1 = null;
                Roster roster2 = null;
                for (Roster r: s.rosters) {
                    if (r.group == previous) {
                        roster1 = r;
                    }

                    else if (r.group == destination) {
                        roster2 = r;
                    }
                }
                if (roster1!=null && roster2!=null) {
                    roster1.addParticipant(person1);
                    roster2.removeParticipant(person1);
                    roster2.addParticipant(person2);
                    roster1.removeParticipant(person2);
                }
            }
        }
    }

    public Solution hillClimb(Solution s) {
        Solution starter = new Solution(s);
        while (true) {
            ArrayList<Swap> swaps = starter.generateSwaps();
            boolean foundBetter = false;

            for (Swap test : swaps) {
                int prevEval = starter.evaluate();
                test.execute(starter);
                int newEval = starter.evaluate();
                if (prevEval > newEval) {
                    foundBetter = true;
                    //System.out.println("I found an improvement xD " + prevEval + " to " + newEval);
                    //starter.printCapacityStats();
                    break;
                } else test.undo(starter);
            }

            if (!foundBetter) {
                break;
            }
        }
        return starter;
    }

    public Solution randomRestarthillClimb(int runs) {
        Solution bestSolution = generateRandom();
        int bestEval = bestSolution.evaluate();
        int i = 0;
        while (i<runs) {
            Solution h = generateRandom();
            h = hillClimb(h);
            int hEval = h.evaluate();
            //System.out.println("Run " + i + " eval is " + hEval + ", best eval is " + bestEval);
            if (hEval<bestEval) {
                bestSolution = h;
                bestEval = hEval;
                //System.out.println("Found a better solution on run " + i);
            }
            else {
                //System.out.println("Did not find a better solution on run " + i);
            }
            i++;
        }
        return bestSolution;
    }

    public Solution simulatedAnnealing(int runs, double initialTemp, double decayConstant) {

        Solution bestSolution = generateRandom();
        int bestEval = bestSolution.evaluate();
        int i = 0;
        while (i<runs) {

            Solution starter = generateRandom();
            double temperature = initialTemp;

            while (true) {
                ArrayList<Swap> swaps = starter.generateSwaps();
                Collections.shuffle(swaps);
                boolean madeChange = false;

                for (Swap test : swaps) {
                    int prevEval = starter.evaluate();
                    test.execute(starter);
                    int newEval = starter.evaluate();
                    //System.out.println("New eval: " + newEval);
                    //System.out.println("Prev eval: " + prevEval);


                    if (newEval < prevEval) { //if this is a good choice, make the change
                        madeChange = true;
                        //System.out.println("I found an improvement xD " + prevEval + " to " + newEval);
                        //starter.printCapacityStats();
                        break;
                    } else if (newEval == prevEval) {
                        boolean doItAnyway = ((-1 / temperature) > Math.random());
                        if (doItAnyway) {
                            madeChange = true;
                            break;
                        } else {
                            madeChange = false;
                            test.undo(starter);
                        }
                    } else {
                        //if probability is higher, we should pick a bad solution
                        boolean pickABadSolution = (Math.exp(((double) (prevEval - newEval)) / temperature) > Math.random());
                        //System.out.println(pickABadSolution);
                        if (pickABadSolution) {
                            //System.out.println("Picked a bad solution: " + prevEval + " to " + newEval);
                            madeChange = true;
                            break;
                        } else {
                            test.undo(starter);
                            madeChange = false;
                        }
                    }
                }
                temperature *= decayConstant;

                if (!madeChange) break;
                //System.out.println("Current eval: " + starter.evaluate());
                //System.out.println("Current temperature: " + temperature);
            }
            int startEval = starter.evaluate();
            if (startEval < bestEval) {
                bestSolution = starter;
                bestEval = startEval;
            }
            i++;
        }
        return bestSolution;
    }

    public static void main(String[] args) throws IOException {
        GuptaOptimize g = new GuptaOptimize();
        g.readFiles("students.csv", "activities.csv", "preferences.csv", "leaders.csv");

        int runs = 10;

        Solution multiSol = g.randomRestarthillClimb(runs);
        System.out.println("Random Restart found a solution:" );
        multiSol.printStats();

        Solution randomSamplingSol = g.randomSampling(runs);
        System.out.println("Random Sampling found a solution:" );
        randomSamplingSol.printStats();

        Solution simAnnealSol = g.simulatedAnnealing(runs,1000, 0.999);
        System.out.println("Simulated Annealing found a solution:" );
        simAnnealSol.printStats();
    }
}