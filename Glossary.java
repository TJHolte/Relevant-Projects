import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Creates a glossary of terms with an index page and term definition pages.
 * Each term definition page has links back to the index and to other term's
 * pages that may be within the definition itself.
 *
 * @author TJ Holte
 *
 */
public final class Glossary {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private Glossary() {
    }

    /**
     * Creates comparator for alphabetically sorting strings, which is default
     * for the compareTo method.
     */
    private static class StringSort implements Comparator<String> {
        @Override

        //establishing a comparator
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Finds terms from input file and puts them into a queue in alphabetical
     * order.
     *
     * @param input
     *            the input file to read from
     * @return the queue of all terms alphabetized
     *
     */

    private static Queue<String> alphabetize(SimpleReader input) {
        Queue<String> alphabetizedQ = new Queue1L<>();
        while (!input.atEOS()) {

            //filling up the queue with every term
            String toQ = input.nextLine();
            alphabetizedQ.enqueue(toQ);
            while (!input.nextLine().isEmpty()) {
                //do nothing, nextLine is already called
            }
        }

        //calling comparator which is set to alphabetize
        Comparator<String> comp = new StringSort();
        alphabetizedQ.sort(comp);

        return alphabetizedQ;
    }

    /**
     * Makes a map of the terms with their definitions.
     *
     * @param input
     *            the input file to read from
     * @return m map of terms with their definitions
     */

    private static Map<String, String> makeMap(SimpleReader input) {
        Map<String, String> m = new Map1L<>();

        while (!input.atEOS()) {

            //the first two lines are normally the term and definition
            String term = input.nextLine();
            String definition = input.nextLine();

            //String "next" is used to detect whether or not the definition has
            //two lines that need to go into the map
            String next = input.nextLine();
            if (!next.isEmpty()) {

                //include the second line
                definition = definition + next;
                input.nextLine();
            }
            m.add(term, definition);
        }

        return m;
    }

    /**
     * Makes HTML site from the generated sets.
     *
     * @param alphabetizedQ
     *            All of the terms in alphabetical order
     * @param mapOfTerms
     *            All of the terms in a map with their values being their
     *            definitions
     * @param out
     *            SimpleWriter
     * @param in
     *            SimpleReader
     * @ensures The glossary index has links to every term listed in
     *          alphabetical order as well as each term having its own page with
     *          all other terms in the definitions having links. Each term page
     *          has a red title and a functioning return to index button.
     */

    private static void makeHTML(Queue<String> alphabetizedQ,
            Map<String, String> mapOfTerms, SimpleWriter out, SimpleReader in) {

        //creating multiple copies of the queue of terms to manipulate
        //within loops
        Queue<String> alphabetizedQCopy = new Queue1L<>();
        Queue<String> alphabetizedQCopy2 = new Queue1L<>();
        alphabetizedQCopy = alphabetizedQ;
        alphabetizedQCopy2 = alphabetizedQ;

        //creating length variables now
        int lengthQ = alphabetizedQCopy.length();
        int lengthQ2 = alphabetizedQCopy2.length();

        //Creating a map for all the words in a definition
        Map<String, String> mapOfWords = new Map1L<>();

        //the variable name of the folder location
        out.println("Enter the name of the folder to store the glossary: ");
        String folder = in.nextLine();

        //making the writer for the index page and specifying its location
        SimpleWriter indexOut = new SimpleWriter1L(folder + "/index.html");

        //initial html
        indexOut.println("<html>");
        indexOut.println("<head>");
        indexOut.print("<title>");
        indexOut.print("Glossary");
        indexOut.println("</title>");
        indexOut.println("</head>");
        indexOut.println("<body>");
        indexOut.println("<h2> TJ's Glossary </h2>");
        indexOut.println("<hr size=\"1\" width=\"100%\" color=\"black\"");
        indexOut.println("<h1> Index </h1>");
        indexOut.println("<ul>");

        //for every term we need to define...
        for (int i = 0; i < lengthQ; i++) {

            //take out the first term from the original alphabetical queue
            //and assign it to a string variable
            String term = alphabetizedQCopy.dequeue();

            //put that term back in, now at the back of the queue
            alphabetizedQCopy.enqueue(term);

            //making the writer for the specific term's page
            SimpleWriter termOut = new SimpleWriter1L(
                    folder + "/" + term + ".html");

            //basic html for the term's page
            termOut.println("<html>");
            termOut.println("<head>");
            termOut.print("<title>");
            termOut.print("Glossary");
            termOut.println("</title>");
            termOut.println("</head>");
            termOut.println("<body>");
            termOut.println("<h2 style=\"color:red\"><i>" + term + "</i></h2>");

            //making a string for the "value," or definition, of the term we're
            //currently building the page of and then copies that into a string
            //builder
            String mDef = mapOfTerms.value(term);
            StringBuilder matchingDef = new StringBuilder(mDef);

            //for every single term that is in the index...
            for (int j = 0; j < lengthQ2; j++) {

                //take out the first term from the original alphabetical queue
                //and assign it to a string variable
                String term2 = alphabetizedQCopy2.dequeue();

                //put that term back in, now at the back of the queue
                alphabetizedQCopy2.enqueue(term2);

                //setting up for the nextWordOrSeparator method
                final String separatorStr = " ,!.?()";
                Set<Character> separatorSet = new Set1L<>();
                generateElements(separatorStr, separatorSet);
                int position = 0;

                //while the current position is less than the length of the
                //definition we're searching for other defined terms in...
                while (position < mDef.length()) {

                    //adds to a map the current word in the definition as the
                    //key and that word as an html file location as the value
                    String token = nextWordOrSeparator(mDef, position,
                            separatorSet);
                    if (!separatorSet.contains(token.charAt(0))) {
                        mapOfWords.add(token,
                                "<a href = \"file:///C:/Users/tjhol/Downloads/"
                                        + "OsuCseWsTemplate/workspace/Glossary/"
                                        + folder + "/" + token + ".html\">"
                                        + token + "</a>");

                        //creating a string with the file location of the
                        //specific word in the definition of the term we're
                        //currently at
                        String definitionLoc = mapOfWords.value(token);

                        //if the word in the definition we're currently on
                        //is the same as the term within the definition we're
                        //looking for
                        if (token.equals(term2)) {

                            int location = matchingDef.indexOf(term2);
                            int locationEnd = location + term2.length();
                            //change the inner term to the file location hyperlink
                            matchingDef.replace(location, locationEnd,
                                    definitionLoc);

                        }
                    }

                    //moves position t0 the next word or separator
                    position += token.length();

                }
            }

            //printing the string builder which now features links to
            //other terms
            termOut.println("<p>" + matchingDef + "</p>");

            //adding a line
            termOut.println("<hr size=\"0.5\" width=\"100%\" color=\"black\">");

            //adding the return to index button
            termOut.println("<p>Return to <a href = \"file:///C:/Users/tjhol/"
                    + "Downloads/OsuCseWsTemplate/workspace/Glossary/" + folder
                    + "/" + "index.html\">index</a></p>");

            //printing the complete term page to the index page
            indexOut.println("<li><a href = \"file:///C:/Users/tjhol/Downloads/"
                    + "OsuCseWsTemplate/workspace/Glossary/" + folder + "/"
                    + term + ".html\">" + term + "</a>" + "</li>");

            termOut.close();

        }
        indexOut.println("</ul>");
        indexOut.close();
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */

    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String answer = "hi";
        int lowest = text.length();

        char c = text.charAt(position);
        boolean b = separators.contains(c);

        //if the character at the position is in the separator set...
        if (b) {

            //look for the first character that's not a separator
            for (int i = position; i < text.length(); i++) {
                char end = text.charAt(i);
                boolean continueBool = separators.contains(end);
                if (!continueBool) {

                    //save the value of the non separator's position
                    if (i < lowest) {
                        lowest = i;
                    }
                }
            }

            //This is the longest group of separators
            answer = text.substring(position, lowest);

        } else {

            //same thing but reversed: go through position until there's a
            //separator
            for (int i = position; i < text.length(); i++) {
                char end = text.charAt(i);
                boolean endBool = separators.contains(end);
                if (endBool) {
                    if (i < lowest) {
                        lowest = i;
                    }
                }
            }

            //This is the longest group of non-separators
            answer = text.substring(position, lowest);
        }

        return answer;
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        charSet.clear();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            charSet.add(c);
        }

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.println("Enter the name of an input file: ");
        String fileName = in.nextLine();
        SimpleReader inputFile = new SimpleReader1L(fileName);
        SimpleReader inputFileCopy = new SimpleReader1L(fileName);
        Queue<String> alphabetizedQ = new Queue1L<>();
        alphabetizedQ = alphabetize(inputFile);
        Map<String, String> mapOfTerms = new Map1L<>();
        mapOfTerms = makeMap(inputFileCopy);
        makeHTML(alphabetizedQ, mapOfTerms, out, in);

        /*
         * Close input and output streams
         */
        in.close();
        out.close();
        inputFile.close();
    }
}
