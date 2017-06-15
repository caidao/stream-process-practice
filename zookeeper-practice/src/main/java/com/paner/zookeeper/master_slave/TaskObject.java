package com.paner.zookeeper.master_slave;

/**
 * Created by paner on 17/6/14.
 */
public class TaskObject {

    private String task;

    private String name;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TaskObject{" +
                "task='" + task + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
