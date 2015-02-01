package com.becmartin.fitfund;

/**
 * Created by sexybexy on 2/1/15.
 */
import java.util.HashMap;
import java.util.Map;


public class Campaign {

    private String school;
    private Double distGoal;
    private Integer freqGoal;
    private String user;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The school
     */
    public String getSchool() {
        return school;
    }

    /**
     *
     * @param school
     * The school
     */
    public void setSchool(String school) {
        this.school = school;
    }

    /**
     *
     * @return
     * The distGoal
     */
    public Double getDistGoal() {
        return distGoal;
    }

    /**
     *
     * @param distGoal
     * The dist_goal
     */
    public void setDistGoal(Double distGoal) {
        this.distGoal = distGoal;
    }

    /**
     *
     * @return
     * The freqGoal
     */
    public Integer getFreqGoal() {
        return freqGoal;
    }

    /**
     *
     * @param freqGoal
     * The freq_goal
     */
    public void setFreqGoal(Integer freqGoal) {
        this.freqGoal = freqGoal;
    }

    /**
     *
     * @return
     * The user
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @param user
     * The user
     */
    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}