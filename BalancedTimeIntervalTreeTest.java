/***************************************************************************
 * COPYRIGHT (C) 2018, Munish Bhatia.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information.
 ***************************************************************************/

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to test the Time Interval Trees
 * and Run Tests Subtracting One List of Time Intervals from Another List of Time Intervals
 */
public class BalancedTimeIntervalTreeTest {

   public static void main(String[] args) {
      BalancedTimeIntervalTreeTest object = new BalancedTimeIntervalTreeTest();

      //Create Array1 of time intervals from which the other list should be subtracted
      TimeInterval[] subtractFrom =
         {new TimeInterval(9,0,10, 00),
         new TimeInterval(8, 30, 9, 15),
         new TimeInterval(8, 00, 8, 15),
         new TimeInterval(8, 45, 8, 55),
         new TimeInterval(7, 00, 7, 15),
         new TimeInterval(8, 20, 8, 25),
         new TimeInterval(11, 45, 12, 50),
         new TimeInterval(10, 30, 10, 45)
         };

      //Create Array of Intervals to be subtracted from Array1 created above
      TimeInterval[] subtractThese =
       { new TimeInterval(8, 0, 9, 0),
         new TimeInterval(9, 0, 10, 20),
         new TimeInterval(10, 20, 11, 00),
         new TimeInterval(13, 15, 15, 00),
         new TimeInterval(11, 10, 12, 15)};

      //Perform (subtractFrom - subtractThese)
      List<TimeInterval> residualList = object.subtractTimeIntervalLists(Arrays.asList(subtractFrom), Arrays.asList(subtractThese));

      //Print Result
      System.out.println("Residual Intervals...");
      if(residualList == null || residualList.size() == 0)
         System.out.println("()");
      else {
         for(TimeInterval t:residualList){
            if(null != t)
               System.out.println(t.toString());
         }
      }
   }

   private List<TimeInterval> subtractTimeIntervalLists(List<TimeInterval> subtractFrom, List<TimeInterval> subtractThis){
      BalancedTimeIntervalTree intervalTree = new BalancedTimeIntervalTree();

      //Load intervals in List 1 (subtractFrom) into an interval tree
      intervalTree.insertIntervals(subtractFrom.toArray(new TimeInterval[subtractFrom.size()]));

      //Subtract intervals in List 2 (subtractThese) from List 1 (subtractFrom) intervals in the interval tree
      for(TimeInterval operand:subtractThis){
         //Get intervals from List 1 overlapping with current interval (operand) from List 2
         List<BalancedTimeIntervalTreeNode> conflictingNodes = getConflictingIntervals(intervalTree, operand);

         //Get interval(s) obtained after subtracting operand from the overlapping list1 intervals obtained in previous step
         List<TimeInterval> adjustedIntervals = getDisjointIntervals(conflictingNodes, operand);

         //Remove conflicting intervals
         for(BalancedTimeIntervalTreeNode node:conflictingNodes)
            intervalTree.deleteInterval(node.interval);

         //Insert adjusted intervals
         for(TimeInterval interval:adjustedIntervals)
            intervalTree.insertInterval(interval);
      }

      return intervalTree.getTreeIntervals();
   }

   private List<BalancedTimeIntervalTreeNode> getConflictingIntervals(BalancedTimeIntervalTree intervalTree, TimeInterval key){
      if(null == key || null == intervalTree)
         return null;

      List<BalancedTimeIntervalTreeNode> overlappingIntervals = intervalTree.getOverlappingIntervals(key);
      return overlappingIntervals;
   }

   private List<TimeInterval> getDisjointIntervals(List<BalancedTimeIntervalTreeNode> conflictingNodes, TimeInterval test) {
      List<TimeInterval> disjointIntervals = new ArrayList<>();
      for(BalancedTimeIntervalTreeNode balancedTimeIntervalTreeNode:conflictingNodes)
         disjointIntervals.addAll(balancedTimeIntervalTreeNode.interval.subtractInterval(test));
      return disjointIntervals;
   }
}

/*
* This class models a Balanced Interval Tree
* Intervals trees are used to represent intervals/ranges
* And to answer queries of the form "which intervals in the tree overlap with a given interval"
* */
class BalancedTimeIntervalTree {
   private BalancedTimeIntervalTreeNode root = null;

   //Inserts a collection of Time Intervals to this Interval Tree
   public BalancedTimeIntervalTreeNode insertIntervals(TimeInterval... intervals){
      for (TimeInterval interval:intervals)
         root = insertInterval(interval);
      return root;
   }

   //Inserts a single time interval to the interval tree
   public BalancedTimeIntervalTreeNode insertInterval(TimeInterval i){
      if(null == i)
         return root;
      if(root == null){
         root = new BalancedTimeIntervalTreeNode(i);
         return root;
      }
      root = insertIntervalUtil(root, i);
      return root;
   }

   //Util method to perform interval insertion
   private BalancedTimeIntervalTreeNode insertIntervalUtil(BalancedTimeIntervalTreeNode root, TimeInterval i){
      //Base Case: Empty SubTree
      if(root == null){
         root = new BalancedTimeIntervalTreeNode(i);
         return root;
      }

      //Determine which side of the root does this node belong to
      if(i.compareTo(root.interval) <= 0)
         root.left = insertIntervalUtil(root.left, i);
      else
         root.right = insertIntervalUtil(root.right, i);

      //Update Max Value of this node if needed
      if(root.max.isBefore(i.end))
         root.max = i.end;

      //Update height of this node if needed
      int leftHeight = root.left!=null?root.left.height:0;
      int rightHeight = root.right!=null?root.right.height:0;
      root.height = Math.max(leftHeight, rightHeight) + 1;

      //Check Balance Factor of Root
      int BalanceFactor = leftHeight - rightHeight;

      //If -1 > BalanceFactor > 1, this node is unbalanced
      //Left-Left Case:
      if(BalanceFactor > 1 && i.compareTo(root.left.interval) <= 0)
         return root.rightRotate();

      //Left-Right Case:
      if(BalanceFactor > 1 && i.compareTo(root.left.interval) > 0){
         root.left = root.left.leftRotate();
         return root.rightRotate();
      }

      //Right-Right Case:
      if(BalanceFactor < -1 && i.compareTo(root.right.interval) > 0)
         return root.rightRotate();

      //Right-Left Case:
      if(BalanceFactor < -1 && i.compareTo(root.right.interval) <= 0){
         root.right = root.right.rightRotate();
         return root.leftRotate();
      }

      return root;
   }

   //Deletes a given time interval from the interval tree
   public BalancedTimeIntervalTreeNode deleteInterval(TimeInterval key){
      if(root == null || key == null)
         return root;
      root = deleteIntervalUtil(root, key);
      return root;
   }

   //Util method to perform interval deletion
   private BalancedTimeIntervalTreeNode deleteIntervalUtil(BalancedTimeIntervalTreeNode root, TimeInterval key) {
      //Base Case: Empty Node Encountered; Interval Does Not Exist In Tree
      if(root == null)
         return root;

      //This is the node to be deleted
      if(root.interval.equals(key)){
         //Base Case (0): Root is a leaf
         if(isLeafNode(root))
            return null;
         //Case 1: Root has no right sub-tree
         else if(!isLeafNode(root) && root.right == null)
            root = root.left;
         else if(!isLeafNode(root) && root.left == null)
            root = root.right;
            //Case 2: Root has right sub-tree
         else{
            //1a. Find, Remove and Return Leftmost node in the curr node's right subtree:
            BalancedTimeIntervalTreeNode successor;
            successor = getSuccessorNode(root.right);
            root.interval = successor.interval;
            root.right = deleteIntervalUtil(root.right, successor.interval);
         }
      }

      else if(key.compareTo(root.interval) <= 0) //Recurse on left subtree
         root.left = deleteIntervalUtil(root.left, key);
      else
         root.right = deleteIntervalUtil(root.right, key);

      //Adjust max if needed
      root.max = TimeInterval.max(root.interval.end, TimeInterval.max(root.left != null?root.left.max:LocalTime.of(0, 0), root.right!=null?root.right.max:LocalTime.of(0, 0)));

      //Update Height of the current Node:
      int leftHeight = root.left!=null?root.left.height:0;
      int rightHeight = root.right!=null?root.right.height:0;
      root.height = Math.max(leftHeight, rightHeight) + 1;

      //Check Node Balance
      int BalanceFactor = leftHeight - rightHeight;

      //Left Left Case:
      if(BalanceFactor > 1 && getBalanceFactor(root.left) >= 0)
         return root.rightRotate();

      //Left Right Case:
      if(BalanceFactor > 1 && getBalanceFactor(root.left) < 0){
         root.left = root.left.leftRotate();
         return root.rightRotate();
      }

      //Right Right Case:
      if(BalanceFactor < -1 && getBalanceFactor(root.right) <= 0)
         return root.leftRotate();

      //Right Left Case:
      if(BalanceFactor < -1 && getBalanceFactor(root.right) > 0){
         root.right = root.right.rightRotate();
         return root.leftRotate();
      }

      return root;
   }

   /*Gets the Balance Factor (difference in heights of left and right subtrees) of a node
   * A leaf node has a height of 1*/
   private int getBalanceFactor(BalancedTimeIntervalTreeNode node) {
      if(node == null)
         return 0;
      int leftHeight = node.left!=null?node.left.height:0;
      int rightHeight = node.right!=null?node.right.height:0;

      return leftHeight-rightHeight;
   }

   //Fetches the time intervals in the interval tree which overlap with the provided time interval 'key'
   List<BalancedTimeIntervalTreeNode> getOverlappingIntervals(TimeInterval key){
      List<BalancedTimeIntervalTreeNode> overlappingIntervals = new ArrayList<>();
      getOverlappingIntervalsUtil(root, key, overlappingIntervals);
      return overlappingIntervals;
   }

   //Fetches the time intervals in the interval tree which overlap with the provided time interval 'key'
   private void getOverlappingIntervalsUtil(BalancedTimeIntervalTreeNode root, TimeInterval key, List<BalancedTimeIntervalTreeNode> overlappingIntervals) {
      //Base Case, Null root or Key
      if(root == null || key == null)
         return;
      if(TimeInterval.doOverlap(root.interval, key)){
         overlappingIntervals.add(root);
         getOverlappingIntervalsUtil(root.left, key, overlappingIntervals);
         getOverlappingIntervalsUtil(root.right, key, overlappingIntervals);
      }
      else{
         if(root.left != null && (root.left.max.isAfter(key.start) || root.left.max.equals(key.start)))
            getOverlappingIntervalsUtil(root.left, key, overlappingIntervals);
         else
            getOverlappingIntervalsUtil(root.right, key, overlappingIntervals);
      }
   }

   //Returns In-order successor of a tree node. This is the leftmost node in the right subtree of a node
   private BalancedTimeIntervalTreeNode getSuccessorNode(BalancedTimeIntervalTreeNode root) {
      if(null == root)
         return null;
      BalancedTimeIntervalTreeNode curr = root, successor;
      while (curr.left != null)
         curr = curr.left;
      successor = curr;
      return successor;
   }

   //Checks if a given tree node is a leaf
   private boolean isLeafNode(BalancedTimeIntervalTreeNode root) {
      return (root == null || (root.left == null && root.right == null));
   }

   //Prints both In-order and Pre-order traversals of the Interval Tree
   void printTree(){
      printTreeInorder();
      printTreePreOrder();
   }

   void printTreeInorder(){
      if(root == null){
         System.out.println("Tree is Empty...");
         return;
      }
      System.out.println("Inorder Tree Traversal:");
      printTreeInorderUtil(root);
   }

   //Prints Inorder Tree Traversal
   private void printTreeInorderUtil(BalancedTimeIntervalTreeNode root) {
      if(null == root)
         return;

      printTreeInorderUtil(root.left);
      System.out.println("[" + root.interval.start + ", " + root.interval.end + "]; Height: " + root.height + "; Max: " + root.max);
      printTreeInorderUtil(root.right);
   }

   void printTreePreOrder(){
      if(root == null){
         System.out.println("Tree is Empty...");
         return;
      }
      System.out.println("PreOrder Tree Traversal...");
      printTreePreOrderUtil(root);
   }

   //Prints Inorder Tree Traversal
   private void printTreePreOrderUtil(BalancedTimeIntervalTreeNode root) {
      if(null == root)
         return;

      System.out.println("[" + root.interval.start + ", " + root.interval.end + "]; Height: " + root.height + "; Max: " + root.max);
      printTreePreOrderUtil(root.left);
      printTreePreOrderUtil(root.right);
   }

   /*Returns a list of Time Intervals encapsulated by the tree nodes.
   * This method uses getTreeIntervalsUtil to recursively fetch the time intervals*/
   List<TimeInterval> getTreeIntervals(){
      if(root == null)
         return null;
      List<TimeInterval> treeIntervals = new ArrayList<>();
      return getTreeIntervalsUtil(root, treeIntervals);
   }

   private List<TimeInterval> getTreeIntervalsUtil(BalancedTimeIntervalTreeNode root, List<TimeInterval> treeIntervals) {
      if(null == root)
         return treeIntervals;
      getTreeIntervalsUtil(root.left, treeIntervals);
      treeIntervals.add(root.interval);
      getTreeIntervalsUtil(root.right, treeIntervals);
      return treeIntervals;
   }
}

/*
* This class represents a Node in the Balanced Interval Tree
* Modelled in the class above
* */
class BalancedTimeIntervalTreeNode{
   TimeInterval interval;
   int height;
   LocalTime max;
   BalancedTimeIntervalTreeNode left, right;

   //Constructor
   BalancedTimeIntervalTreeNode(TimeInterval i) {
      this.interval = i;
      max = i.end;
      height = 1;
      left = null;
      right = null;
   }

   /*Performs Left Rotation of the Tree Node
   * This is used for height balancing the Interval Tree
   * */
   BalancedTimeIntervalTreeNode leftRotate(){
      BalancedTimeIntervalTreeNode rightChild = this.right;
      BalancedTimeIntervalTreeNode leftChildOfRightChild = rightChild.left;

      //Rotate
      rightChild.left = this;
      this.right = leftChildOfRightChild;

      //Update Heights
      this.height = Math.max(this.left!=null?this.left.height:0, this.right!=null?this.right.height:0) + 1;
      if(rightChild != null)
         rightChild.height = Math.max(rightChild.left!=null?rightChild.left.height:0, rightChild.right!=null?rightChild.right.height:0) + 1;

      //Update Max Values
      this.max = TimeInterval.max(this.interval.end, TimeInterval.max(this.left!=null?this.left.max:LocalTime.of(0, 0), this.right!=null?this.right.max:LocalTime.of(0,0)));
      if(rightChild != null)
         rightChild.max = TimeInterval.max(rightChild.interval.end, TimeInterval.max(rightChild.left!=null?rightChild.left.max:LocalTime.of(0, 0), rightChild.right!=null?rightChild.right.max:LocalTime.of(0,0)));

      //Return New Root
      return rightChild;
   }

   /*Performs Right Rotation of the Tree Node
    * This is used for height balancing the Interval Tree
    * */
   BalancedTimeIntervalTreeNode rightRotate(){
      BalancedTimeIntervalTreeNode leftChild = this.left;
      BalancedTimeIntervalTreeNode rightChildOfLeftChild = leftChild.right;

      //Rotate
      leftChild.right = this;
      this.left = rightChildOfLeftChild;

      //Update Heights
      this.height = Math.max(this.left!=null?this.left.height:0, this.right!=null?this.right.height:0) + 1;
      if(leftChild != null)
         leftChild.height = Math.max(leftChild.left!=null?leftChild.left.height:0, leftChild.right!=null?leftChild.right.height:0) + 1;

      //Update Max Values
      this.max = TimeInterval.max(this.interval.end, TimeInterval.max(this.left!=null?this.left.max:LocalTime.of(0, 0), this.right!=null?this.right.max:LocalTime.of(0,0)));
      if(leftChild != null)
         leftChild.max = TimeInterval.max(leftChild.interval.end, TimeInterval.max(leftChild.left!=null?leftChild.left.max:LocalTime.of(0, 0), leftChild.right!=null?leftChild.right.max:LocalTime.of(0,0)));

      //Return New Root
      return leftChild;
   }
}

/*
* This class represents a Time Interval Instance
* */
class TimeInterval implements Comparable{
   LocalTime start, end;

   //Constructor
   TimeInterval(LocalTime start, LocalTime end) {
      if(start.isAfter(end))
         throw new IllegalArgumentException("Start Time Cannot be After End Time In an Interval.");
      this.start = start;
      this.end = end;
   }

   //Constructor
   TimeInterval(Integer startHour, Integer startMinutes, Integer endHour, Integer endMinutes){
      this.start = LocalTime.of(startHour, startMinutes);
      this.end = LocalTime.of(endHour, endMinutes);
      if(start.isAfter(end))
         throw new IllegalArgumentException("Start Time Cannot be After End Time In an Interval.");
   }

   /*
   * Subtracts One Interval From Another
   * E.g. (9:00-9:30) - (9:00-9:15) = (9:15-9:30)
   * (9:00-10:00) - (8:30-10:15) = ()
   * (9:00-9:30) - (10:00-11:00) = (9:00-9:30)
   * */
   List<TimeInterval> subtractInterval(TimeInterval op){
      List<TimeInterval> result = new ArrayList<>();
      if(this.containsWithin(op)){
         TimeInterval interval1 = new TimeInterval(this.start, op.start);
         if(!interval1.isEmptyInterval())
            result.add(interval1);

         TimeInterval interval2 = new TimeInterval(op.end, this.end);
         if(!interval2.isEmptyInterval())
            result.add(interval2);
      }
      else if(op.containsWithin(this))
         result.add(null);
      else {  //partial overlap
         if(this.beginsSoonerThan(op))
            result.add(new TimeInterval(this.start, op.start));
         else
            result.add(new TimeInterval(op.end, this.end));
      }
      return result;
   }

   //Checks if 'this' time interval begins sooner than (or before) time interval op
   private boolean beginsSoonerThan(TimeInterval op) {
      return (op == null || this.start.isBefore(op.start));
   }

   //Checks if time interval 'i' is contained within time interval 'this'
   //For the purposes of this method interval (9:00-9:30) is contained within another interval with same range i.e. (9:00-9:30)
   private boolean containsWithin(TimeInterval i){
      return (i != null && isBeforeOrAt(this.start, i.start) && isBeforeOrAt(i.end, this.end));
   }

   //Checks if the start and end time of the interval are the same
   private boolean isEmptyInterval() {
      return (this.start.equals(this.end));
   }

   //Checks if Interval 'i' starts before or at the same time as Interval 'j'
   private boolean isBeforeOrAt(LocalTime i, LocalTime j){
      return (i.isBefore(j) || i.equals(j));
   }

   //Returns max of time intervals 'i' and 'j'
   public static LocalTime max(LocalTime t1, LocalTime t2){
      return (t1.isAfter(t2)?t1:t2);
   }

   //Checks if intervals 'i' and 'j' overlap
   public static boolean doOverlap(TimeInterval i, TimeInterval j){
      return (i != null && j != null && i.start.isBefore(j.end) && i.end.isAfter(j.start));
   }

   @Override
   public int compareTo(Object o) {
      if(null == o || (!(o instanceof TimeInterval)))
         return -1;
      TimeInterval op = (TimeInterval)o;
      if(this.beginsSoonerThan(op))
         return -1;
      else if(op.beginsSoonerThan(this))
         return +1;
      else
         return 0;
   }

   @Override
   public String toString() {
      return "(" + start + "-" + end + ")";
   }

   @Override
   public boolean equals(Object obj) {
      if(!(obj instanceof  TimeInterval))
         return false;
      TimeInterval o = (TimeInterval)obj;
      return (this.end.equals(o.end) && this.start.equals(o.start));
   }
}
