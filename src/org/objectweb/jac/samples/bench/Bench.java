package org.objectweb.jac.samples.bench;

import java.lang.reflect.Method;

public class Bench {
    /**
     * <p>Simple benchmark suite to mesure impact of wrappers</p>
     * <p>Usage: <code>java org.objectweb.jac.samples.bench.Bench &lt;n&gt;</code>
     *
     * <p>This sample is a benchmark to test the impact of wrappers on
     * performances. There are 8 different tests for each iteration,
     * testing different calls.</p>
     */
    public static void main(String args[]) throws Exception {
        if (args.length<1 || args.length>2) {
            System.err.println("Wrong number of arguments");
            printUsage();
            System.exit(1);
        }
        Bench b = new Bench(
            Integer.parseInt(args[0]),
            args.length > 1 ? args[1] : "d");
      
        try {
            m1=Bench.class.getMethod("m1",new Class[]{});
            m2=Bench.class.getMethod("m2",new Class[]{int.class});
            m3=Bench.class.getMethod("m3",new Class[]{int.class,int.class});
            m4=Bench.class.getMethod("m4",new Class[]{});
            m5=Bench.class.getMethod("m5",new Class[]{int.class});
            m6=Bench.class.getMethod("m6",new Class[]{int.class,int.class});
            m7=Bench.class.getMethod("m7",new Class[]{Object.class});
            m8=Bench.class.getMethod("m8",new Class[]{Object.class,Object.class});
        } catch(Exception e) {
            e.printStackTrace();
        }
        // Let the VM optimize
        System.out.print("Warming up ");
        b.doBench(); System.out.print(".");
        b.doBench(); System.out.print(".");
        b.doBench(); System.out.print(".");
        b.doBench(); System.out.println(".");
        //Thread.currentThread().sleep(5000);

        System.out.println("Starting bench");
        long total = 0;
        int num_runs = 5;
        for (int i=0; i<num_runs; i++) {
            long start = System.currentTimeMillis();
            b.doBench();
            long end = System.currentTimeMillis();
            System.out.print(i+": "+(end-start)+"ms; ");
            total += end-start;
        }
        System.out.println("	average="+(total/num_runs));
    }

    static void printUsage() {
        System.out.println("Usage: java org.objectweb.jac.samples.bench.Bench <nb iterations> [s|r]");
        System.out.println("	<nb iterations>: number of iterations");
        System.out.println("	[r|s]: r(reflective calls), d(direct calls)");
    }

    boolean rCalls;

    void doBench() {
        if(rCalls) {
            // reflexive calls
            bench1();
            bench2();
            bench3();
            bench4();
            bench5();
            bench6();
            bench7();
            bench8();
        } else {
            // direct calls
            sbench1();
            sbench2();
            sbench3();
            sbench4();
            sbench5();
            sbench6();
            sbench7();
            sbench8();
        }
    }

    static Method m1;
    static Method m2;
    static Method m3;
    static Method m4;
    static Method m5;
    static Method m6;
    static Method m7;
    static Method m8;

    public int n;
    String s;

    /**
    * @param n numbers of method calls for the benchs.
    * @param reflectiveCalls argument that indicates if the call must
    * be reflexive ("r") or direct ("d")
    */
    public Bench(int n,String reflectiveCalls) {
        this.n = n;
        if(reflectiveCalls.equals("r")) {
            this.rCalls=true;
        } else {
            this.rCalls=false;
        }
    }

    /**
    * Method with no argument returning void
    */
    void bench1() {
        for(int i=0; i<n; i++) {
            try {
                m1.invoke(this,new Object[]{});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench1() {
        for(int i=0; i<n; i++) {
            m1();
        }
    }

    public void m1() {
        int i=0;
    }


    /**
    * Method with 1 argument returning void
    */
    void bench2() {
        for(int i=0; i<n; i++) {
            try {
                m2.invoke(this,new Object[]{new Integer(0)});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench2() {
        //System.out.print("bench 2: ");
        for(int i=0; i<n; i++) {
            m2(0);
        }
    }

    public void m2(int a) {
        int i=0;
    }

    /**
    * Method with 2 argument returning void
    */
    void bench3() {
        for(int i=0; i<n; i++) {
            try {
                m3.invoke(this,new Object[]{new Integer(0),new Integer(0)});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench3() {
        for(int i=0; i<n; i++) {
            m3(0,0);
        }
    }

    public void m3(int a, int b) {
        int i=0;
    }

    /**
    * Method with no argument returning an int
    */
    void bench4() {
        for(int i=0; i<n; i++) {
            try {
                m4.invoke(this,new Object[]{});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench4() {
        for(int i=0; i<n; i++) {
            m4();
        }
    }

    public int m4() {
        return 0;
    }

    /**
    * Method with 1 argument returning an int
    */
    void bench5() {
        for(int i=0; i<n; i++) {
            try {
                m5.invoke(this,new Object[]{new Integer(0)});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench5() {
        for(int i=0; i<n; i++) {
            m5(0);
        }
    }

    public int m5(int a) {
        return 0;
    }

    /**
    * Method with 1 argument returning an int
    */
    void bench6() {
        for(int i=0; i<n; i++) {
            try {
                m6.invoke(this,new Object[]{new Integer(0),new Integer(0)});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench6() {
        for(int i=0; i<n; i++) {
            m6(0,0);
        }
    }

    public int m6(int a, int b) {
        return 0;
    }

    /**
    * Method with 1 argument returning an int
    */
    void bench7() {
        for(int i=0; i<n; i++) {
            try {
                m7.invoke(this,new Object[]{null});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench7() {
        for(int i=0; i<n; i++) {
            m7(null);
        }
    }

    public void m7(Object a) {
        return;
    }


    /**
    * Method with 1 argument returning an int
    */
    void bench8() {
        for(int i=0; i<n; i++) {
            try {
                m8.invoke(this,new Object[]{null,null});
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    void sbench8() {
        for(int i=0; i<n; i++) {
            m8(null,null);
        }
    }

    public void m8(Object a, Object b) {
        return;
    }

}
