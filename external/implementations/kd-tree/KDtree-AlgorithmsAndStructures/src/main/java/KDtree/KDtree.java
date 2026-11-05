package KDtree;

import DataTypes.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class KDtree <T extends ITreeData<T, K>, K> {
    private TreeNode<T, K> root = null;
    private int currentId = 0;
    private int maxDimension;

    public KDtree(int dimension) {
        this.maxDimension = dimension;

    }

    public TreeNode<T, K> insert(T data) {
        if (this.root == null) {
            this.createRoot(data);
            return root;
        } else {
            int dimension = 0;
            TreeNode<T, K> currentNode = this.root;
            boolean notFound = true;
            while (notFound) {
                if (currentNode.getListOfData().isEmpty()) {
                    currentNode.insertData(data);
                    notFound = false;
                } else {
                    switch(data.compareTo(currentNode.getListOfData().getFirst().getKey(), dimension)) {
                        case -1:
                            currentNode = currentNode.createLeftChild();
                            break;
                        case 0:
                            currentNode.insertData(data);
                            notFound = false;
                            break;
                        case 1:
                            currentNode = currentNode.createRightChild();
                            break;
                    }
                }

                dimension = (dimension + 1) % maxDimension;
            }
            return currentNode;
        }
    }
    private void createRoot(T data) {
        this.root = new TreeNode<T, K>();
        this.root.insertData(data);
    }

    private ArrayList<T> findAll(K key) {
        if (this.root != null) {
            int dimension = 0;
            TreeNode<T, K> currentNode = this.root;
            boolean notFound = true;
            while (notFound && currentNode != null) {
                switch(currentNode.getListOfData().getFirst().compareTo(key, dimension)) {
                    case 1:
                        currentNode = currentNode.getLeftChild();
                        break;
                    case 0:
                        notFound = false;
                        return currentNode.getListOfData();
                    case -1:
                        currentNode = currentNode.getRightChild();
                        break;
                }

                dimension = (dimension + 1) % maxDimension;
            }
        }
        return new ArrayList<T>();
    }
    public T findOne(T data) {
        ArrayList<T> listOfAll = this.findAll(data.getKey());
        for (T pieceOfData : listOfAll) {
            if (pieceOfData.equals(data)) { // najdenie zhody, nie len podla kluca
                return pieceOfData;
            }
        }
        return null;
    }

    public ArrayList<T> findAllExact(K key) {
        ArrayList<T> listOfAll = this.findAll(key);
        ArrayList<T> returnList = new ArrayList<>();
        for (T pieceOfData : listOfAll) {
            if (pieceOfData.keyEqual(key)) {
                returnList.add(pieceOfData);
            }
        }
        return returnList;
    }

    public boolean edit(T oldData, T newData) {
        if (this.root != null) {
            int dimension = 0;
            TreeNode<T, K> currentNode = this.root;
            boolean notFound = true;
            while (notFound && currentNode != null) {
                switch(currentNode.getListOfData().getFirst().compareTo(oldData.getKey(), dimension)) {
                    case 1:
                        currentNode = currentNode.getLeftChild();
                        break;
                    case 0:
                        notFound = false;
                        for (T searchData : currentNode.getListOfData()) {
                            if (searchData.equals(oldData)){ // najdenie zhody
                                this.deletePrivate(searchData, currentNode,dimension);
                            }
                        }

                    case -1:
                        currentNode = currentNode.getRightChild();
                        break;
                }

                dimension = (dimension + 1) % maxDimension;
            }
        }
       this.insert(newData);
        return true;
    }

    public boolean deletePublic(T data) {
        if (this.root != null) {
            int dimension = 0;
            TreeNode<T, K> currentNode = this.root;
            boolean notFound = true;
            while (notFound && currentNode != null) {
                switch(currentNode.getListOfData().getFirst().compareTo(data.getKey(), dimension)) {
                    case 1:
                        currentNode = currentNode.getLeftChild();
                        break;
                    case 0:
                        notFound = false;
                        for (T searchData : currentNode.getListOfData()) {
                            if (searchData.equals(data)){
                                this.deletePrivate(searchData, currentNode,dimension); //najdenie toho presne isteho objektu
                                return true;
                            }
                        }

                    case -1:
                        currentNode = currentNode.getRightChild();
                        break;
                }

                dimension = (dimension + 1) % maxDimension;
            }
        }
        return false;
    }
    private void deletePrivate(T data, TreeNode<T, K> node, int currentDimension) {
        if (node.getListOfData().size() >= 2) {
            node.getListOfData().remove(data); // v node je viac objektov s rocnakym klucom, mozem jednoducho prvok vyhodit zo zoznamu
        } else {
            if (!node.hasChildren()) {
                //je list
                node.getListOfData().clear();
                TreeNode<T, K> parent = node.getParent();

                if (parent != null) {
                    //vymažeme refernciu na daný node z parenta, čím sa nose vymaže.
                    if (parent.getRightChild() == node) {
                        parent.setRightChild(null);
                    } else {
                        parent.setLeftChild(null);
                    }
                } else {
                    // Node je root
                    root = null;
                }
            } else {
                //nie je list
                TreeNode<T, K> currentNode = node;
                T dataDelete = node.getListOfData().getFirst();
                TreeNode<T, K> nodeDelete = node;
                int replacementDimension = currentDimension;
                NodeWithDimension<T, K> replacementNode = findReplacement(node, replacementDimension); //najdenie minima alebo maxima
                boolean needToDeleteOtherKeysAndInsert = false;
                if (replacementNode.numberOfUsages > 1) { // ci je potrebne mazat a znova vkladat prvky s rovnakym klucom
                    needToDeleteOtherKeysAndInsert = true;
                    dataDelete = replacementNode.node.getListOfData().getFirst();
                }
                while (replacementNode != null) { //prehliadam pokym mam co premiestnovat medzi nodes
                    if (replacementNode.node.getListOfData().size() >= 2) {
                        ArrayList<T> listOfData;
                        if (currentNode.getRightChild() != null) {
                            listOfData = replacementNode.node.findList(true, replacementDimension);
                        } else {
                            listOfData = replacementNode.node.findList(false, replacementDimension);
                        }
                        if (listOfData.size() == replacementNode.node.getListOfData().size()) {
                            currentNode.getListOfData().clear(); //vymazeme vsetko z mazaneho nodu
                            currentNode.getListOfData().addAll(replacementNode.node.getListOfData()); // do mazaneho nodu sa premiestna data
                            if (!replacementNode.node.hasChildren()) {
                                //je list
                                TreeNode<T, K> leafNode = replacementNode.node;
                                leafNode.getListOfData().clear();
                                TreeNode<T, K> parent = leafNode.getParent();
                                if (parent != null) {
                                    if (parent.getRightChild() == leafNode) {
                                        parent.setRightChild(null);
                                    } else {
                                        parent.setLeftChild(null);
                                    }
                                } else {
                                    // Node je root
                                    root = null;
                                }
                                replacementNode = null;
                            } else {
                                replacementDimension = replacementNode.dimension;
                                currentNode = replacementNode.node;
                                replacementNode = findReplacement(currentNode, replacementDimension); // pokracujem v premiestovani prvkov medzi nodes
                            }
                        } else {
                            //ak nepremiestnujem vsetky prvky z najdeneho node node
                            currentNode.getListOfData().clear();//vymazeme vsetko z mazaneho nodu
                            currentNode.getListOfData().addAll(listOfData);// do mazaneho nodu sa premiestna data
                            replacementNode.node.getListOfData().removeAll(listOfData); //vymazeme z najdeneho nodu presnutu data
                            replacementNode = null;
                        }

                    } else {
                        currentNode.getListOfData().clear(); //vymazeme vsetko z mazaneho nodu
                        currentNode.getListOfData().addAll(replacementNode.node.getListOfData()); // do mazaneho nodu sa premiestna data
                        if (!replacementNode.node.hasChildren()) {
                            //je list
                            TreeNode<T, K> leafNode = replacementNode.node;
                            leafNode.getListOfData().clear();
                            TreeNode<T, K> parent = leafNode.getParent();
                            if (parent != null) {
                                if (parent.getRightChild() == leafNode) {
                                    parent.setRightChild(null);
                                } else {
                                    parent.setLeftChild(null);
                                }
                            } else {
                                // Node je root
                                root = null;
                            }
                            replacementNode = null;
                        } else {
                            replacementDimension = replacementNode.dimension;
                            currentNode = replacementNode.node;
                            replacementNode = findReplacement(currentNode, replacementDimension);
                        }
                    }

                }
                if (needToDeleteOtherKeysAndInsert) {
                    this.deleteAndInsert(dataDelete, node, currentDimension);
                }

            }
        }
        data.deleteSelf(); // clean up (referencii)
    }
    //vymazanie rovankych kluc v podstromoch a ich opatovne vlozenie
private void deleteAndInsert(T data, TreeNode<T, K> node, int currentDimension) {
        Queue<NodeWithDimension<T, K>> nodes = new LinkedList<>();
    NodeWithDimension<T, K> currentNode = new NodeWithDimension<T, K>(node, currentDimension);
    if (node.getRightChild() != null) {
        nodes.offer(new NodeWithDimension<T, K>(node.getRightChild(), (currentDimension + 1) % maxDimension));

    } else if (node.getLeftChild() != null) {
        nodes.offer(new NodeWithDimension<T, K>(node.getLeftChild(), (currentDimension + 1) % maxDimension));
    }
    while (!nodes.isEmpty()) {
        NodeWithDimension<T, K> current = nodes.poll();
        int vysledok = currentNode.node.getListOfData().getFirst().compareTo(data.getKey(), currentDimension);
        if ( vysledok == 0 ) { // ma rovnaky kluc ako v hladanej dimenzii
            T dataToDeleteAndInsert = node.getListOfData().getFirst();
            this.deletePublic(dataToDeleteAndInsert);
            this.insert(dataToDeleteAndInsert);

        }
        //pridam do prehladavania len ak nie som na hladanej dimenzii a zaroven nie je vacsi
        if (current.node.getLeftChild() != null && !(current.dimension == currentDimension && vysledok == 1)) {
            nodes.offer(new NodeWithDimension<T,K>(current.node.getLeftChild(), (currentDimension + 1) % maxDimension));
        }
        //pridam do prehladavania len ak nie som na hladanej dimenzii a zaroven nie je mensi
        if (current.node.getRightChild() != null  && !(current.dimension == currentDimension && vysledok == -1)) {
            nodes.offer(new NodeWithDimension<T,K>(current.node.getRightChild(), (currentDimension + 1) % maxDimension));
        }

    }

}

//maximum v lavom podstrome alebo minimum v pravom, preferuje sa pravy podstrom
private NodeWithDimension<T, K> findReplacement(TreeNode<T, K> node, int dimension) {
    NodeWithDimension<T, K> returnNode = null;
    if (node.getRightChild() != null) {
        returnNode =  findMinNode(node.getRightChild(), dimension);
    } else if (node.getLeftChild() != null) {
        returnNode = findMaxNode(node.getLeftChild(), dimension);
    }
    if (returnNode == null) {
        return null;
    } else if (returnNode.node == node) {
        return null;
    }
     return returnNode;

}

    private NodeWithDimension<T, K> findMinNode(TreeNode<T, K> node, int targetDimension) {

        int minNodeDimension = (targetDimension+1) % maxDimension;
        NodeWithDimension<T, K> maxWrapped = new NodeWithDimension<T, K>(node, minNodeDimension);
        Queue<NodeWithDimension<T, K>> queue = new LinkedList<>();
        queue.add(new NodeWithDimension<T, K>(node, minNodeDimension));
        T currentMinData = node.getListOfData().getFirst();
        while (!queue.isEmpty()) {
            NodeWithDimension<T, K> currentNodeWithDim = queue.poll();
            TreeNode<T, K> currentNode = currentNodeWithDim.node;
            int nodeDim = currentNodeWithDim.dimension;


            for (T data: currentNodeWithDim.node.getListOfData()) {
                if (data.compareTo(currentMinData.getKey(), targetDimension) < 0) { // hladame minimum tak minus jeden to musi vratit
                    maxWrapped.node = currentNode;
                    maxWrapped.dimension = nodeDim;
                    maxWrapped.numberOfUsages = 1;
                    currentMinData = data;
                } else if (data.compareTo(currentMinData.getKey(), targetDimension) == 0) { // nasli sme prvok s rovnakym klucom podla dimenzie (duplicita)
                    maxWrapped.numberOfUsages =  maxWrapped.numberOfUsages + 1;
                }
            }

            if (currentNode.getLeftChild() != null )
                queue.add(new NodeWithDimension<T, K>(currentNode.getLeftChild(), (nodeDim + 1) % maxDimension));

            if (currentNode.getRightChild() != null && nodeDim != targetDimension) // ked som na node ma rovnaku dimenziu ako mazany prvok tak uz nemusim hladat dany podstrom
                queue.add(new NodeWithDimension<T, K>(currentNode.getRightChild(), (nodeDim + 1) % maxDimension));
        }
        return maxWrapped;
    }

    private NodeWithDimension<T, K> findMaxNode(TreeNode<T, K> node, int targetDimension) {
        int maxNodeDimension = (targetDimension+1)% maxDimension;
        NodeWithDimension<T, K> maxWrapped = new NodeWithDimension<T, K>(node, maxNodeDimension);
        Queue<NodeWithDimension<T, K>> queue = new LinkedList<>();
        queue.add(new NodeWithDimension<T, K>(node, maxNodeDimension));
        T currentMaxData = node.getListOfData().getFirst();
        while (!queue.isEmpty()) {
            NodeWithDimension<T, K> currentNodeWithDim = queue.poll();
            TreeNode<T, K> currentNode = currentNodeWithDim.node;
            int nodeDim = currentNodeWithDim.dimension;

            for (T data: currentNodeWithDim.node.getListOfData()) {
                if (data.compareTo(currentMaxData.getKey(), targetDimension) > 0) { // hladame minimum tak plus jeden to musi vratit
                    maxWrapped.node = currentNode;
                    maxWrapped.dimension = nodeDim;
                    maxWrapped.numberOfUsages = 1;
                    currentMaxData = data;
                } else if (currentMaxData.compareTo(data.getKey(), targetDimension) == 0) { // nasli sme prvok s rovnakym klucom podla dimenzie (duplicita)
                    maxWrapped.numberOfUsages =  maxWrapped.numberOfUsages + 1;
                }
            }

            if (currentNode.getLeftChild() != null && nodeDim != targetDimension) // ked som na node ma rovnaku dimenziu ako mazany prvok tak uz nemusim hladat dany podstrom
                queue.add(new NodeWithDimension<T, K>(currentNode.getLeftChild(), (nodeDim + 1) % maxDimension));

            if (currentNode.getRightChild() != null )
                queue.add(new NodeWithDimension<T, K>(currentNode.getRightChild(), (nodeDim + 1) % maxDimension));
        }
        return maxWrapped;
    }


    public int numberOfData() {
        //level order prehliadka
        int count = 0;
        if (root != null) {

            Queue<TreeNode<T, K>> nodes = new LinkedList<>();
            nodes.offer(this.root);
            while (!nodes.isEmpty()) {
                int size = nodes.size();
                for (int i = 0; i < size; i++) {
                    TreeNode<T, K> current = nodes.poll();
                    count+=current.getListOfData().size();
                    if (current.getLeftChild() != null) {
                        nodes.offer(current.getLeftChild());
                    }
                    if (current.getRightChild() != null) {
                        nodes.offer(current.getRightChild());
                    }
                }
            }
        }
        return count;
    }
    public ArrayList<T> allData() {
        //level order prehliadka
        ArrayList<T> treeData = new ArrayList<>();
        int count = 0;
        if (root != null) {

            Queue<TreeNode<T, K>> nodes = new LinkedList<>();
            nodes.offer(this.root);
            while (!nodes.isEmpty()) {
                int size = nodes.size();
                for (int i = 0; i < size; i++) {
                    TreeNode<T, K> current = nodes.poll();
                    treeData.addAll(current.getListOfData());
                    if (current.getLeftChild() != null) {
                        nodes.offer(current.getLeftChild());
                    }
                    if (current.getRightChild() != null) {
                        nodes.offer(current.getRightChild());
                    }
                }
            }
        }
        return treeData;
    }

    private void createRoot() {
        this.root = new TreeNode<T, K>();
    }

    public int getCurrentId() {
        return currentId;
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public int getMaxDimension() {
        return maxDimension;
    }
    public void clear() {
        this.root = null;
    }
    //obalovacia treda, ktora sluzi na najdenie nodu s menima alebo maximom podla urcenej dimenzie
    private static class NodeWithDimension<T extends ITreeData<T, K>, K> {
        TreeNode<T, K> node;
        int dimension;
        int numberOfUsages;

        NodeWithDimension(TreeNode<T, K> node, int dimension) {
            this.node = node;
            this.dimension = dimension;
            this.numberOfUsages = 0;
        }
    }
}

