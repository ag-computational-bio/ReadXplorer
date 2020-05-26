/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.common.parser.data.embl;

import java.util.Objects;

/**
 * Representation of the DT tag.
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class Date {

    public Date() {
    }

    public Date(Entry createDate, Entry updateDate) {
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public static class Entry {

        private java.util.Date date;
        private Integer revision;
        private Integer version;

        public Entry() {
        }

        public Entry(java.util.Date date, Integer revision, Integer version) {
            this.date = date;
            this.revision = revision;
            this.version = version;
        }

        public java.util.Date getDate() {
            return date;
        }

        public void setDate(java.util.Date date) {
            this.date = date;
        }

        public Integer getRevision() {
            return revision;
        }

        public void setRevision(Integer revision) {
            this.revision = revision;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + Objects.hashCode(this.date);
            hash = 53 * hash + Objects.hashCode(this.revision);
            hash = 53 * hash + Objects.hashCode(this.version);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.date, other.date)) {
                return false;
            }
            if (!Objects.equals(this.revision, other.revision)) {
                return false;
            }
            if (!Objects.equals(this.version, other.version)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Entry{" + "date=" + date + ", revision=" + revision + ", version=" + version + '}';
        }

    }

    private Date.Entry createDate;
    private Date.Entry updateDate;

    public Date.Entry getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date.Entry createDate) {
        this.createDate = createDate;
    }

    public Date.Entry getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date.Entry updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.createDate);
        hash = 97 * hash + Objects.hashCode(this.updateDate);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Date other = (Date) obj;
        if (!Objects.equals(this.createDate, other.createDate)) {
            return false;
        }
        if (!Objects.equals(this.updateDate, other.updateDate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Date{" + "createDate=" + createDate + ", updateDate=" + updateDate + '}';
    }

}
