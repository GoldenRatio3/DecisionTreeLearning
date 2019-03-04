import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

class ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1]
	 *      children[2] is the branch for attribute 3 == strings[3][2]
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class TreeNode {

		TreeNode[] children;
		int value;

		public TreeNode(TreeNode[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()
		
		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class TreeNode

	private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private TreeNode decisionTree;	// Tree learnt in training, used for classifying
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute

	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor
	
	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);
	
	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		// for each row classify
		for(int i=1; i<testData.length; i++) {
			String[] row = testData[i];
			// for each attribute
			System.out.println(transverse(row, decisionTree, 0));
		}
	} // classify()


	/**
	 * Takes a row of data to be classified and transverses the tree to find the
	 * correct classification
	 */
	public String transverse(String[] testData, TreeNode currentNode, int attrPos) {
		if(currentNode.children == null) {
			return strings[attributes-1][currentNode.value];
		} else {
			// for each child
			for(int i=0; i<currentNode.children.length; i++) {
				if(testData[currentNode.value].equals(strings[currentNode.value][i])) {
					return transverse(testData, currentNode.children[i], ++attrPos);
				}
			}
			return "Tree incomplete";
		}
	}

	public void train(String[][] trainingData) {
		indexStrings(trainingData);
		// starts with an no attributes currently used
		ArrayList<Integer> usedAttributes = new ArrayList<>();
		decisionTree = buildTree(trainingData, usedAttributes);

	} // train()

	/**
	 * Recursive method
	 * builds the decision tree to be used in classification
	 */
	public TreeNode buildTree(String[][] data, ArrayList<Integer> removedAttributes) {	
		if(checkIfPure(data)) {
			// build and return tree node -- Integer.parseInt(data[1][data[0].length-1])
			int indexPos = -1;
			for(int i=0; i<stringCount[stringCount.length-1]; i++) {
					if(strings[strings.length-1][i].equals(data[1][data[0].length-1])) {
						indexPos = i;
						break;
					}
			}
			return new TreeNode(null, indexPos);
		} else if(removedAttributes.size() == attributes-1) {
			// no more attributes left
			return new TreeNode(null, mostCommon(data));
		} else {
			int[] convertedRemovedAttributes = new int[removedAttributes.size()];
			for(int i=0; i<convertedRemovedAttributes.length; i++)
				convertedRemovedAttributes[i] = removedAttributes.get(i).intValue();
			int bestAttribute = findBestAttribute(data, convertedRemovedAttributes);
			TreeNode newNode = new TreeNode(null, bestAttribute);
			removedAttributes.add(bestAttribute);
			ArrayList<TreeNode> children = new ArrayList<>();
			// build tree for each subnode 

			for(int i=0; i<stringCount[bestAttribute]; i++) {
				children.add(buildTree(getSubset(data, bestAttribute, strings[bestAttribute][i]), removedAttributes));
			}
			TreeNode[] ch = new TreeNode[children.size()];
			for(int i=0; i<ch.length; i++)
				ch[i] = children.get(i);

			newNode.children = ch;
			removedAttributes.remove(removedAttributes.size()-1);
			return newNode;
		}
	}

	/**
	 * Gets a subset of the data to be used in next iteration of buildTree method
	 */
	String[][] getSubset(String[][] data, int attr, String attrValue) {
		ArrayList<String[]> subset = new ArrayList<String[]>();
		subset.add(data[0]); // add attributes

		// create subset
		for(int i=1; i<data.length; i++) {
			if(data[i][attr].equals(attrValue)) {
				subset.add(data[i]);
			}
		} // End for

		// convert arraylist to string[][]
		String[][] convertedSubset = new String[subset.size()][data[0].length];
		for(int i=0; i<subset.size(); i++) {
			convertedSubset[i] = subset.get(i);
		}
		return convertedSubset;

	}

	/**
	 * Checks to see if the given data is pure
	 */
	boolean checkIfPure(String[][] data) {
		if(data != null) {
			String value = data[1][data[0].length-1];
			char[] temp1 = value.toCharArray();
			for(int i=1; i<data.length; i++) {
				if(!(value.trim()).equals((data[i][data[0].length-1]).trim()))
					return false;
			} 
			return true;
		}
		return false;
	}

	/**
	 * Takes the subset data and returns the most common classification
	 */
	int mostCommon(String[][] data) {
		int[] possibleValues = new int[stringCount[stringCount.length-1]];
		for(int i=0; i<possibleValues.length; i++)
			possibleValues[i] = 0;

		for(int i=1; i<data.length; i++) {
			int current = possibleValues[Integer.parseInt(data[i][data[0].length-1])];
			current++;
			possibleValues[Integer.parseInt(data[i][data[0].length-1])] = current;
		}

		// find most common
		int total = -1;
		int value = -1;
		for(int i=0; i<possibleValues.length; i++) {
			if(possibleValues[i] > total) {
				total = possibleValues[i];
				value = i;
			}
		}
		if(value != -1)
			return value;
		else
			return -1;
	}

	/**
	 * Uses entropy and calculateGain method to find the best attribute
	 * to split on at any given point
	 */
	int findBestAttribute(String[][] trainingData, int[] removedAttributes) {
		int bestAttribute = -1;
		double bestGain = -1;
		ArrayList<String[]> bestSubsets = new ArrayList<String[]>();

		// Find best attribute
		for(int attr=0; attr<attributes-1; attr++) {
			if(isIn(removedAttributes, attr))
				continue;
			// calculate root entropy
			double rootEntropy = calculateEntropy(trainingData);
			ArrayList<Double> childEntropies = new ArrayList<>();
			ArrayList<Integer> weightedSize = new ArrayList<>();

			// for all attributes values
			for(int attrValues = 0; attrValues<stringCount[attr]; attrValues++) {
				ArrayList<String[]> subset = new ArrayList<String[]>();
				int count = 0;
				subset.add(trainingData[0]); // add attributes

				// create subset
				for(int i=1; i<trainingData.length; i++) {
					if(trainingData[i][attr].equals(strings[attr][attrValues])) {
						count++;
						subset.add(trainingData[i]);
					}
				} // End for
				weightedSize.add(count);

				// convert arraylist to string[][]
				String[][] convertedSubset = new String[subset.size()][3];
				for(int i=0; i<subset.size(); i++) {
					convertedSubset[i] = subset.get(i);
				}
				// calculate entropy for this subset
				childEntropies.add(calculateEntropy(convertedSubset));
			}

			// work out gain and compare
			double currentGain = calculateGain(rootEntropy, childEntropies, weightedSize, trainingData.length-1);
			if(currentGain > bestGain) {
				// best attribute so far
				bestGain = currentGain;
				bestAttribute = attr;
			}
		}

		return bestAttribute;
	}

	/**
	 * Helper method to see if a value is in the arrayB
	 */
	boolean isIn(int[] arr, int val) {
		if(arr != null) {
			for(int i=0; i<arr.length; i++) {
				if(arr[i] == val)
					return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Calculate the entropy of a given set
	 * @return Entropy
	 */
	double calculateEntropy(String[][] data) {
		double entropy = 0;
		int count = data.length-1; // -1 because of the class names
		int pos = 0;
		if(data.length == 0)
			return 0;
		// getting required values for calculation
		for(int i=0; i<data.length; i++) {
			if(data[i][data[0].length-1].equals(strings[strings.length-1][0]))
				pos++;
		}
		// E(S) = -xlogx(P+) - xlogx(P-)
		// P- = 1 - P+
		entropy = -xlogx((double)pos/(double)count) - xlogx(1-((double)pos/(double)count));
		return entropy;

	} // calculateEntropy()

	/**
	 * Calculates the gain from using a specific attribute
	 * @return gain
	 */
	double calculateGain(double rootEntropy, ArrayList<Double> childEntropies, ArrayList<Integer> weightedSize, int total) {
		double gain = rootEntropy;
		// run through child entropies to calculate gain
		for(int i=0; i<childEntropies.size(); i++) {
			gain -= ((weightedSize.get(i)/(double)total) * childEntropies.get(i));
		}

		return gain;

	} // calculateGain()

	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()
		
	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3
