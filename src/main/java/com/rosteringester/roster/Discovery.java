package com.rosteringester.roster;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by jeshernandez on 07/21/2017.
 */
public class Discovery {


    private List<String> firstName; // Medicare Required
    private List<String> lastName; // Medicare Required
    private List<String> role; // Medicare Required
    private List<String> specialty; // Medicare Required
    private List<String> npi; // Medicare Required
    private List<String> groupName; // Medicare Required
    private List<String> address; // Medicare Required
    private List<String> suite; // Medicare Required
    private List<String> city; // Medicare Required
    private List<String> state; // Medicare Required
    private List<String> zip; // Medicare Required
    private List<String> servicePhone; // Medicare Required
    private List<String> officeHours; // Medicare Required
    private List<String> practiceAgeLimits; // Medicare Required
    private List<String> acceptingNewPatients; // Medicare Required
    private List<String> providerType; // Medicare Required
    Logger log = Logger.getLogger(Discovery.class.getName());

    // ---------------------------------
    public static class Builder {

        private List<String> firstName;
        private List<String> lastName;
        private List<String> role;
        private List<String> specialty;
        private List<String> npi;
        private List<String> groupName;
        private List<String> address;
        private List<String> suite;
        private List<String> city;
        private List<String> state;
        private List<String> zip;
        private List<String> servicePhone;
        private List<String> officeHours;
        private List<String> practiceAgeLimits;
        private List<String> acceptingNewPatients;
        private List<String> providerType;


        public Builder firstName(List<String> firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(List<String> lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder role(List<String> role) {
            this.role = role;
            return this;
        }

        public Builder specialty(List<String> specialty) {
            this.specialty = specialty;
            return this;
        }

        public Builder npi(List<String> npi) {
            this.npi = npi;
            return this;
        }

        public Builder groupName(List<String> groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder address(List<String> address) {
            this.address = address;
            return this;
        }

        public Builder suite(List<String> suite) {
            this.suite = suite;
            return this;
        }

        public Builder city(List<String> city) {
            this.city = city;
            return this;
        }

        public Builder state(List<String> state) {
            this.state = state;
            return this;
        }

        public Builder zip(List<String> zip) {
            this.zip = zip;
            return this;
        }

        public Builder servicePhone(List<String> servicePhone) {
            this.servicePhone = servicePhone;
            return this;
        }

        public Builder officeHours(List<String> officeHours) {
            this.officeHours = officeHours;
            return this;
        }

        public Builder practiceAgeLimits(List<String> practiceAgeLimits) {
            this.practiceAgeLimits = practiceAgeLimits;
            return this;
        }

        public Builder acceptingNewPatients(List<String> acceptingNewPatients) {
            this.acceptingNewPatients = acceptingNewPatients;
            return this;
        }

        public Builder providerType(List<String> providerType) {
            this.providerType = providerType;
            return this;
        }


        // Create build
        public Discovery build() {
            return new Discovery(this);
        }

    } // End of Builder


    private Discovery(Builder builder) {
        firstName = builder.firstName;
        lastName = builder.lastName;
        role = builder.role;
        specialty = builder.specialty;
        npi = builder.npi;
        groupName = builder.groupName;
        address = builder.address;
        suite = builder.suite;
        city = builder.city;
        state = builder.state;
        zip = builder.zip;
        servicePhone = builder.servicePhone;
        officeHours = builder.officeHours;
        practiceAgeLimits = builder.practiceAgeLimits;
        acceptingNewPatients = builder.acceptingNewPatients;
        providerType = builder.providerType;

        log.info("Discovery Builder Complete.");
    }



    // ------------GETTER------------
    public List<String> getFirstName() {
        return firstName;
    }
    public List<String> getLastName() {
        return lastName;
    }

    public List<String> getRole() {
        return role;
    }

    public List<String> getSpecialty() {
        return specialty;
    }

    public List<String> getNpi() {
        return npi;
    }

    public List<String> getGroupName() {
        return groupName;
    }

    public List<String> getAddress() {
        return address;
    }

    public List<String> getSuite() {
        return suite;
    }

    public List<String> getCity() {
        return city;
    }

    public List<String> getState() {
        return state;
    }

    public List<String> getZip() {
        return zip;
    }

    public List<String> getServicePhone() {
        return servicePhone;
    }

    public List<String> getOfficeHours() {
        return officeHours;
    }

    public List<String> getPracticeAgeLimits() {
        return practiceAgeLimits;
    }

    public List<String> getAcceptingNewPatients() {
        return acceptingNewPatients;
    }

    public List<String> getProviderType() {
        return providerType;
    }


} // End of Discovery class
