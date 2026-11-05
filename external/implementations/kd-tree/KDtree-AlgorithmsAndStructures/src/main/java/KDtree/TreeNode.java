package KDtree;

import DataTypes.*;

import java.util.ArrayList;

public class TreeNode<T extends ITreeData<T, K>, K> {

    private TreeNode<T, K> parent;
    private TreeNode<T, K> leftChild;
    private TreeNode<T, K> rightChild;
    private ArrayList<T> listOfData;


    public TreeNode() {
        this.listOfData = new ArrayList<>();
    }

    public TreeNode<T, K> getLeftChild() {
        return leftChild;
    }

    public TreeNode<T, K> getRightChild() {
        return rightChild;
    }

    public TreeNode createLeftChild() {
        //ak už exituje potomok, tak ho vráti, inak vráti nového potomka
        if (this.leftChild == null) {
            this.leftChild = new TreeNode<T, K>();
            this.leftChild.setParent(this);
        }
        return this.leftChild;
    }

    public TreeNode createRightChild() {
        //ak už exituje potomok, tak ho vráti, inak vráti nového potomka
        if (this.rightChild == null) {
            this.rightChild = new TreeNode<T, K>();
            this.rightChild.setParent(this);
        }
        return this.rightChild;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public void insertData(T data) {
        this.listOfData.add(data);
    }

    public ArrayList<T> getListOfData() {
        return listOfData;
    }

    public boolean hasChildren() {
       if (this.rightChild == null && this.leftChild == null) {
           return false;
       } else {
           return  true;
       }
    }

    public void setLeftChild(TreeNode<T, K> leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(TreeNode<T, K> rightChild) {
        this.rightChild = rightChild;
    }

    public ArrayList<T> findList(boolean isRight, int dimension) {

        if (isRight) {
            return this.findMinNode(dimension);
        } else {
            return this.findMaxNode(dimension);
        }

    }

    private ArrayList<T> findMaxNode(int dimension) {
        ArrayList<T> listOfData = new ArrayList<>();
        T currentMax = this.listOfData.getFirst();
        for (T data: this.listOfData) {
            if (data.compareTo(currentMax.getKey(), dimension) > 0) {
                currentMax = data;
                listOfData.clear();
                listOfData.add(data);
            } else if (data.compareTo(currentMax.getKey(), dimension) == 0) {
                listOfData.add(data);
            }
        }
        return listOfData;
    }

    private ArrayList<T> findMinNode(int dimension) {
        ArrayList<T> listOfData = new ArrayList<>();
        T currentMin = this.listOfData.getFirst();
        for (T data: this.listOfData) {
            if (data.compareTo(currentMin.getKey(), dimension) < 0) {
                currentMin = data;
                listOfData.clear();
                listOfData.add(data);
            } else if (data.compareTo(currentMin.getKey(), dimension) == 0) {
                listOfData.add(data);
            }
        }
        return listOfData;
    }


}
