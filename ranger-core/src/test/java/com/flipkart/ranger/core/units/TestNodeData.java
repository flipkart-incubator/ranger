package com.flipkart.ranger.core.units;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestNodeData {
    private int nodeId;

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null || obj.getClass()!= this.getClass())
            return false;
        TestNodeData that = (TestNodeData) obj;
        return (that.nodeId == this.nodeId);
    }

    @Override
    public int hashCode(){
        return this.nodeId;
    }

}
