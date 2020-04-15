package com.me.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Test {

    public static void main(String[] args) {

        //泛型的好处是使用时不必对类型进行强制转换，它通过编译器对类型进行检查
        // 可以省略后面的Number，编译器可以自动推断泛型类型：
        ArrayList<String> strList = new ArrayList<>();
        strList.add("hello"); // OK
        String s = strList.get(0); // OK
        //strList.add(new Integer(123)); // compile error!
        //Integer n = strList.get(0); // compile error!

        //ArrayList<T>实现了List<T>接口，它可以向上转型为List<T>
        List<String> sList = strList;

        //Arrays.sort(Object[])可以对任意数组进行排序，
        // 但待排序的元素必须实现Comparable<T>这个泛型接口
        Person[] ps = new Person[]{
                new Person("Bob", 61),
                new Person("Alice", 88),
                new Person("Lily", 75),
        };
        Arrays.sort(ps);
        System.out.println(Arrays.toString(ps));

        //泛型还可以定义多种类型。Java标准库的Map<K, V>就是使用两种泛型类型的例子.
        Map<String, Person> map = new HashMap<>();
        map.put("a", new Person("aaa", 80));

        //Java语言的泛型实现方式是擦拭法（Type Erasure）。
        //所谓擦拭法是指，虚拟机对泛型其实一无所知，所有的工作都是编译器做的。
        //Java使用擦拭法实现泛型，导致了：
        //编译器把类型<T>视为Object；
        //编译器根据<T>实现安全的强制转型。
        //Java的泛型是由编译器在编译时实行的，编译器内部永远把所有类型T视为Object处理，
        // 但是，在需要转型的时候，编译器会根据T的类型自动为我们实行安全地强制转型。
        //Java泛型的局限：
        //局限一：<T>不能是基本类型，例如int，因为实际类型是Object，Object类型无法持有基本类型：
        //Pair<int> p = new Pair<>(1, 2); // compile error!
        //局限二：无法取得带泛型的Class
        //无论T的类型是什么，getClass()返回同一个Class实例，因为编译后它们全部都是Pair<Object>
        Pair<String> p1 = new Pair<>("Hello", "world");
        Pair<Integer> p2 = new Pair<>(123, 456);
        Class c1 = p1.getClass();
        Class c2 = p2.getClass();
        System.out.println(c1 == c2); // true
        System.out.println(c1 == Pair.class); // true
        //局限三：无法判断带泛型的Class：
        // Compile error:
        //if (p2 instanceof Pair<String>.class) { ... }
        //局限四：不能实例化T类型：
        /*
        class Pair<T> {
            private T first;
            private T last;

            // Compile error:
            public Pair() {
                //first = new T();
                //last = new T();
            }
            //要实例化T类型，我们必须借助额外的Class<T>参数：
            public Pair(Class<T> clazz) {
                first = clazz.newInstance();
                last = clazz.newInstance();
            }
        }
         */

        //在继承了泛型类型的情况下，子类可以获取父类的泛型类型
        Class<IntPair> clazz = IntPair.class;
        Type t = clazz.getGenericSuperclass();  //此对象表示的类的直接超类
        if (t instanceof ParameterizedType) {   //ParameterizedType表示参数化类型
            ParameterizedType pt = (ParameterizedType) t;
            Type[] types = pt.getActualTypeArguments(); //返回一个Type对象的数组，表示此类型的实际类型参数
            Type firstType = types[0]; // 取第一个泛型类型
            Class<?> typeClass = (Class<?>) firstType;
            System.out.println(typeClass); // Integer
        }

        int n = add(p2);
        System.out.println(n);

        People<Number> people1 = null;
        People<Integer> people2 = new People<>(1, 2);
        People<Double> people3 = null;
        //People<String> people4 = null;//// compile error!

        Pair<Number> p11 = new Pair<>(12.3, 4.56);
        Pair<Integer> p22 = new Pair<>(123, 456);
        setSame(p11, 100);
        setSame(p22, 200);
        System.out.println(p11.getFirst() + ", " + p11.getLast());
        System.out.println(p22.getFirst() + ", " + p22.getLast());

        //<?>通配符有一个独特的特点，就是：Pair<?>是所有Pair<T>的超类
        Pair<?> pp = p2;
    }

    //Java的泛型还允许使用无限定通配符（Unbounded Wildcard Type），即只定义一个?：
    //不允许调用set(T)方法并传入引用（null除外）；
    //不允许调用T get()方法并获取T引用（只能获取Object引用）
    static boolean isNull(Pair<?> p) {
        return p.getFirst() == null || p.getLast() == null;
    }

    /*
    用PECS原则：Producer Extends Consumer Super。
    即：如果需要返回T，它是生产者（Producer），要使用extends通配符；
    如果需要写入T，它是消费者（Consumer），要使用super通配符
     */
    //Pair<? super Integer>表示，方法参数接受所有泛型类型为Integer 或 Integer父类 的Pair类型
    static void setSame(Pair<? super Integer> p, Integer n) {
        p.setFirst(n);
        p.setLast(n);
        //不允许调用get()方法获得Integer的引用。
        //唯一例外是可以获取Object的引用：Object o = p.getFirst()。
        Object obj = p.getFirst();
    }


    // 使用<? extends Number>的泛型定义称之为上界通配符（Upper Bounds Wildcards），
    // 即把泛型类型T的上界限定在Number了。
    // 由此可以实现只读的方法，避免恶意修改
    static int add(Pair<? extends Number> p) {
        Number first = p.getFirst();
        Number last = p.getLast();
        //方法参数签名setFirst(? extends Number)无法传递任何Number类型给setFirst(? extends Number)。
        //这里唯一的例外是可以给方法参数传入null
        //p.setFirst(new Integer(first.intValue() + 100));
        //p.setLast(new Integer(last.intValue() + 100));
        //p.setFirst(null); // ok, 但是后面会抛出NullPointerException
        //p.getFirst().intValue(); // NullPointerException
        return first.intValue() + last.intValue();
    }
}

//可以在接口中定义泛型类型，实现此接口的类必须实现正确的泛型类型。
class Person implements Comparable<Person> {
    String name;
    int score;

    Person(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public int compareTo(Person other) {
        return this.name.compareTo(other.name);
    }

    public String toString() {
        return this.name + "," + this.score;
    }
}

class Pair<T> {
    private T first;
    private T last;

    public Pair(T first, T last) {
        this.first = first;
        this.last = last;
    }

    /* 泛型类型<T>不能用于静态方法,会导致编译错误:
    public static Pair<T> create(T first, T last) {
        return new Pair<T>(first, last);
    }
     */
    // 静态泛型方法应该使用其他类型区分:
    //这个<K>和Pair<T>类型的<T>已经没有任何关系了
    public static <K> Pair<K> create(K first, K last) {
        return new Pair<K>(first, last);
    }

    public T getFirst() {
        return first;
    }

    public T getLast() {
        return last;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setLast(T last) {
        this.last = last;
    }
}

//一个类可以继承自一个泛型类。
class IntPair extends Pair<Integer> {
    public IntPair(Integer first, Integer last) {
        super(first, last);
    }
}

//在定义泛型类型People<T>的时候，也可以使用extends通配符来限定T的类型：
class People<T extends Number> {

    private T a;
    private T b;

    public People(T a, T b) {
        this.a = a;
        this.b = b;
    }
}