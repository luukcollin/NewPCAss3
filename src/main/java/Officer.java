import java.util.List;

public class Officer implements JMSConnection{
    private List<Integer> priorityQueue;
    public Officer(String topic){
        //topic.publish(top;ic)

    }

    public void generateMergerQueues(){

        //Hierin geeft de officer aan welke node head/tail zijn.
        //Dit gebeurt a.d.h.v. hi en lo var.
    }

    public void retrieveHiLoAndPivotsFromNodes(){

    }

    public void retrieveLows(){

    }
    public void retrieveHighs(){

    }
    public void retrievePivots(){

    }


    private void assignMergeTask(int nodeId1, int nodeId2){

    }

    public int compare(){
        return -1;
    }
}
