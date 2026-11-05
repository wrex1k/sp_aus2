package DataTypes;

public interface ITreeData<T extends ITreeData<T, K>, K> {
    int compareTo(K key, int dimension);
    boolean equals (T data);
    boolean keyEqual(K key);
    K getKey() ;
    void deleteSelf();
}
