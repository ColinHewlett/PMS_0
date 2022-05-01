/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.model;

/**
 *
 * @author colin
 */
public class TableRowValue extends EntityStoreType{
    private Integer value = null;
    
    public TableRowValue(Integer key){
        setIsTableRowValue(true);
        setValue(key);
    }
    
    public Integer getValue(){
        return value;
    }
    
    public void setValue(Integer value){
        this.value = value;
    }
}
