# DecisionTreeLearning
Implementation of the ID3 Algorithm in Java

Artificial Intelligence:

I implemented the algorithm as follows;
	•	Calculate the entropy of every attribute in the set
	•	Find the best attribute by using information gain and split into subsets accordingly
	•	Create a decision tree node containing the attribute
	•	Recurse on each subset with the remaining attributes

My main methods include;
	•	buildTree – which recursively builds the decision tree. Checks to see if data is pure if so returns corresponding classification, if not then finds best attribute to split on and call buildTree on each subset.
	•	findBestAttribute – takes the set and an int array of used attributes and calculates the entropy and information gain for each attribute then returns the attribute with the highest information gain.
	•	calculateEntropy – calculates the entropy of a given set.
	•	calculateGain – calculate gain of a given attribute.
	•	getSubset – helper method to split the original set into subsets.
	•	checkIfPure – another helper method to go through the set and see if all have the same classification
	•	mostCommon – goes through the set and returns the most common classification

To allow for any number of classes I used the private class attributes wherever possible.

Further Improvements; 
	•	If I had more time I would have liked to implement pruning to allow for better efficiency.
