package id.ac.itb.todolist.dao;

import id.ac.itb.todolist.model.Attachment;
import id.ac.itb.todolist.model.Category;
import id.ac.itb.todolist.model.Tugas;
import id.ac.itb.todolist.model.User;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class TugasDao extends DataAccessObject {

    public Tugas getTugas(int idTugas, boolean retTags, boolean retAttachment, boolean retAssignees) {
        Tugas tugas = null;
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT t.`id` AS `id`, t.`nama` AS `nama`, `tgl_deadline`,  `status` , t.`last_mod` AS `last_mod`, t.`pemilik` AS `pemilik_username`, u.`email` AS `pemilik_email`, u.`password` AS `pemilik_password`, u.`full_name` AS `pemilik_full_name`, u.`tgl_lahir` AS `pemilik_tgl_lahir`, u.`avatar` AS `pemilik_avatar`, c.`id` AS `kategori_id`, c.`nama` AS `kategori_nama`, c.`last_mod` AS `kategori_last_mod` FROM `categories` c, `tugas` t, `users` u WHERE t.`id` = ? AND c.`id` = t.`id_kategori` AND u.`username` = t.`pemilik`;");
            preparedStatement.setInt(1, idTugas);

            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                tugas = new Tugas();

                tugas.setId(rs.getInt("id"));
                tugas.setNama(rs.getString("nama"));
                tugas.setTglDeadline(rs.getDate("tgl_deadline"));
                tugas.setStatus(rs.getBoolean("status"));
                tugas.setLastMod(rs.getTimestamp("last_mod"));

                User pemilik = new User();
                pemilik.setUsername(rs.getString("pemilik_username"));
                pemilik.setEmail(rs.getString("pemilik_email"));
                pemilik.setHashedPassword(rs.getString("pemilik_password"));
                pemilik.setFullName(rs.getString("pemilik_full_name"));
                pemilik.setTglLahir(rs.getDate("pemilik_tgl_lahir"));
                pemilik.setAvatar(rs.getString("pemilik_avatar"));
                tugas.setPemilik(pemilik);

                Category kategori = new Category();
                kategori.setId(rs.getInt("kategori_id"));
                kategori.setNama(rs.getString("kategori_nama"));
                kategori.setLastMod(rs.getTimestamp("kategori_last_mod"));
                tugas.setKategori(kategori);

                if (retTags) {
                    tugas.setTags(getTags(idTugas));
                }

                if (retAttachment) {
                    tugas.setAttachments(getAttachments(idTugas));
                }

                if (retAssignees) {
                    tugas.setAssignees(getAssignees(idTugas));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tugas;
    }
    
    public Collection<User> getAssignees(int idTugas) {
        ArrayList<User> result = null;
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT a.`username` AS `username`, u.`full_name` AS `full_name`, u.`avatar` AS `avatar`, u.`email` AS `email`, u.`password` AS `password`, u.`tgl_lahir` AS `tgl_lahir` FROM `assignees` a, `users` u WHERE a.`id_tugas`=? AND a.`username`=u.`username`;");
            preparedStatement.setInt(1, idTugas);

            ResultSet rs = preparedStatement.executeQuery();
            
            result = new ArrayList<User>();
            while (rs.next()) {
                User item = new User();
                item.setUsername(rs.getString("username"));
                item.setEmail(rs.getString("email"));
                item.setHashedPassword(rs.getString("password"));
                item.setFullName(rs.getString("full_name"));
                item.setTglLahir(rs.getDate("tgl_lahir"));
                item.setAvatar(rs.getString("avatar"));
                                
                result.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Collection<Attachment> getAttachments(int idTugas) {
        ArrayList<Attachment> result = null;
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT `id_attachment`, `id_tugas`, `name`, `filename`, `type` FROM attachments WHERE id_tugas=?");
            preparedStatement.setInt(1, idTugas);

            ResultSet rs = preparedStatement.executeQuery();

            result = new ArrayList<Attachment>();
            while (rs.next()) {
                Attachment item = new Attachment();
                item.setIdAttachment(rs.getInt("id_attachment"));
                item.setIdTugas(rs.getInt("id_tugas"));
                item.setName(rs.getString("name"));
                item.setFilename(rs.getString("filename"));
                item.setType(rs.getString("type"));

                result.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Collection<String> getTags(int idTugas) {
        ArrayList<String> result = null;
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT tag FROM tags WHERE id_tugas=?");
            preparedStatement.setInt(1, idTugas);

            ResultSet rs = preparedStatement.executeQuery();

            result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("tag"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isUpdated(int idTugas, long lastRequest) {
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT COUNT(*) AS n FROM `tugas` WHERE `id` = ? AND `last_mod` > FROM_UNIXTIME(?);");
            preparedStatement.setInt(1, idTugas);
            preparedStatement.setLong(2, lastRequest);
            
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            if (rs.getInt("n") > 0) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean isPemilik(int idTugas, String username) {
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT * FROM `tugas` WHERE `id_tugas`=? AND `pemilik`=? LIMIT 0, 1");
            preparedStatement.setInt(1, idTugas);

            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isAssignees(int idTugas, String username) {
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("'SELECT * FROM `tugas` t NATURAL JOIN `assignees` a ON t.`id` = a.`id_tugas` WHERE t.`id` = ? AND a.`username` = ?'");
            preparedStatement.setInt(1, idTugas);
            preparedStatement.setString(2, username);

            ResultSet rs = preparedStatement.executeQuery();
            
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public int setStats(int idTugas, int n) {
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("UPDATE `tugas` SET `status` = ? WHERE `id` = ?;");
            preparedStatement.setInt(1, n);
            preparedStatement.setInt(2, idTugas);
            
            return preparedStatement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int deleteTask(int idTugas) {
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("DELETE FROM `tugas` WHERE id=?;");
            preparedStatement.setInt(1, idTugas);
            
            return preparedStatement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int AddTask(String nama,Date deadline, String pemilik, int id){
        try {
            
            PreparedStatement preparedStatement = connection.
                    prepareStatement("INSERT INTO tugas(nama, tgl_deadline, pemilik,id_kategori) VALUES (?, ?, ?, ?);");
            preparedStatement.setString(1, nama);
            preparedStatement.setDate(2, deadline);
            preparedStatement.setString(3, pemilik);
            preparedStatement.setInt(4, id);
            int x= preparedStatement.executeUpdate();
            return x;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int addAssignee(int id,String nama){
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("INSERT INTO `assignees`(`id_tugas`, `username`) VALUES (?,?)");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, nama);
            
            return preparedStatement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int getNewestId()
    {
        int x=-1;
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT id FROM tugas ORDER BY last_mod DESC LIMIT 1");
            
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next())
            {
                x=rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return x;
    }
    
    public int addTag(int id,String nama){
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("INSERT INTO `tags`(`id_tugas`, `tag`) VALUES (?,?);");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, nama);
            return preparedStatement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public ArrayList<Tugas> getAllTugas(){
        ArrayList<Tugas> result = null;
        Tugas tugas = null;
        try {
            PreparedStatement preparedStatement = connection.
                    prepareStatement("SELECT t.`id` AS `id`, t.`nama` AS `nama`, `tgl_deadline`,  `status` , t.`last_mod` AS `last_mod`, t.`pemilik` AS `pemilik_username`, u.`email` AS `pemilik_email`, u.`password` AS `pemilik_password`, u.`full_name` AS `pemilik_full_name`, u.`tgl_lahir` AS `pemilik_tgl_lahir`, u.`avatar` AS `pemilik_avatar`, c.`id` AS `kategori_id`, c.`nama` AS `kategori_nama`, c.`last_mod` AS `kategori_last_mod` FROM `categories` c, `tugas` t, `users` u WHERE  c.`id` = t.`id_kategori` AND u.`username` = t.`pemilik`;");

            ResultSet rs = preparedStatement.executeQuery();
            
            result = new ArrayList<Tugas>();
            while (rs.next()) {
                tugas = new Tugas();

                tugas.setId(rs.getInt("id"));
                tugas.setNama(rs.getString("nama"));
                tugas.setTglDeadline(rs.getDate("tgl_deadline"));
                tugas.setStatus(rs.getBoolean("status"));
                tugas.setLastMod(rs.getTimestamp("last_mod"));

                User pemilik = new User();
                pemilik.setUsername(rs.getString("pemilik_username"));
                pemilik.setEmail(rs.getString("pemilik_email"));
                pemilik.setHashedPassword(rs.getString("pemilik_password"));
                pemilik.setFullName(rs.getString("pemilik_full_name"));
                pemilik.setTglLahir(rs.getDate("pemilik_tgl_lahir"));
                pemilik.setAvatar(rs.getString("pemilik_avatar"));
                tugas.setPemilik(pemilik);

                Category kategori = new Category();
                kategori.setId(rs.getInt("kategori_id"));
                kategori.setNama(rs.getString("kategori_nama"));
                kategori.setLastMod(rs.getTimestamp("kategori_last_mod"));
                tugas.setKategori(kategori);
                
                tugas.setTags(getTags(rs.getInt("id")));
                tugas.setAttachments(getAttachments(rs.getInt("id")));
                tugas.setAssignees(getAssignees(rs.getInt("id")));
                
                result.add(tugas);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
