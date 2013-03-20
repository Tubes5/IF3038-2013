<?php

class Tugas extends Model
{
	private $_id_tugas;
	private $_taskname;
	private $_attachment;
	private $_tgl_deadline;
	private $_status;
	private $_last_mod;
	private $_pemilik;
	private $_tag;
	private $_kategori;
	private $_assignees;
	
	public function get_id_tugas() { return $this->_id_tugas; } 
	public function get_taskname() { return $this->_taskname; } 
	public function get_attachment() { return $this->_attachment; } 
	public function get_attachment2($i) { return $this->_attachment[$i]; } 
	public function get_tgl_deadline() { return $this->_tgl_deadline; } 
	public function get_status() { return $this->_status; } 
	public function get_last_mod() { return $this->_last_mod; } 
	public function get_pemilik() { return $this->_pemilik; } 
	public function get_kategori() { return $this->_kategori; } 
	public function get_id_kategori() { return $this->_id_kategori; } 
	public function get_tag() { return $this->_tag; } 
	public function get_tag2($i) { return $this->_tag[$i]; } 
	public function set_id_tugas($x) { $this->_id_tugas = ($x); } 
	public function set_taskname($x) { $this->_taskname = $x; } 
	public function set_attachment($x) { $this->_attachment = $x; } 
	public function set_attachment2($i,$x) { $this->_attachment[$i] = $x; } 
	public function set_tgl_deadline($x) { $this->_tgl_deadline = $x; } 
	public function set_status($x) { $this->_status = $x; } 
	public function set_last_mod($x) { $this->_last_mod = $x; } 
	public function set_pemilik($x) { $this->_pemilik = $x; } 
	public function set_kategori($x) { $this->_kategori = $x; } 
	public function set_id_kategori($x) { $this->_id_kategori = $x; } 
	public function set_tag($x) { $this->_tag = $x; } 
	public function set_tag2($i,$x) { $this->_tag[$i] = $x; } 
	
	public function fromArray($tugas)
	{
		$this->_id_tugas = $tugas["id"];
		$this->_taskname = $tugas["nama"];
		$this->_attachment = $tugas["attachment"];
		$this->_tgl_deadline = $tugas["tgl_deadline"];
		$this->_status = $tugas["status"];
		$this->_last_mod = $tugas["last_mod"];
		$this->_pemilik = $tugas["pemilik"];
		$this->_tag = $tugas["tag"];
		$this->_id_kategori = $tugas["id_kategori"];
		$this->_kategori = $tugas["kategori"];
	}
	
	public function toArray()
	{
		$tugas = array();
		$tugas["id"] = $this->_id_tugas;
		$tugas["nama"] = $this->_taskname;
		$tugas["attachment"] = $this->_attachment;
		$tugas["tgl_deadline"] = $this->_tgl_deadline;
		$tugas["status"] = $this->_status;
		$tugas["last_mod"] = $this->_last_mod;
		$tugas["pemilik"] = $this->_pemilik;
		$tugas["tag"] = $this->_tag;
		$tugas["id_kategori"] = $this->_id_kategori;
		$tugas["kategori"] = $this->_kategori;
		$tugas["assignees"] = $this->_assignees;
		
		return $tugas;
	}
	
	public function getTugas($id_tugas)
	{
		$sql = "SELECT t.id AS id, t.nama AS nama, tgl_deadline,  `status` , t.last_mod AS last_mod, pemilik, id_kategori, c.nama AS nama_kategori
				FROM categories c, tugas t
				WHERE t.id = ? AND c.id = t.id_kategori;";

		$this->_setSql($sql);
		
		$tugas = $this->getRow(array($id_tugas));
		 
		if (empty($tugas))
		{
			return false;
		}
		
		$this->_id_tugas = $tugas["id"];
		$this->_taskname = $tugas["nama"];
		$this->_tgl_deadline = $tugas["tgl_deadline"];
		$this->_status = $tugas["status"];
		$this->_last_mod = $tugas["last_mod"];
		$this->_pemilik = $tugas["pemilik"];
		$this->_id_kategori = $tugas["id_kategori"];
		$this->_kategori = $tugas["nama_kategori"];

		$this->_tag = $this->getTags($id_tugas);
		$this->_attachment = $this->getAttachments($id_tugas);
		$this->_assignees = $this->getAssignees($id_tugas);

		return $this->toArray();
	}
	
	public function getTags($id_tugas)
	{
		$sql = "SELECT tag FROM tags WHERE id_tugas=? ";
		$this->_setSql($sql);
		
		$r = $this->getAll(array($id_tugas));
		$retval = array();
		
		$i = 0;
		while(!empty($r[$i]["tag"]))
		{
			$retval[$i] = $r[$i]["tag"];
			$i++;
		}

		return $retval;
	}

	public function getAssignees($id_tugas)
	{
		$sql = "SELECT a.username AS username, u.full_name AS full_name, u.avatar AS avatar FROM assignees a, users u WHERE id_tugas=? AND a.username=u.username";
		$this->_setSql($sql);
		return $this->getAll(array($id_tugas));
	}
	
	public function getAttachments($id_tugas)
	{
		$sql = "SELECT id_attachment, name, filename, type FROM attachments WHERE id_tugas=?";
		$this->_setSql($sql);
		return $this->getAll(array($id_tugas));
	}


	public function setStats($i,$n)
    {
        $sql = "UPDATE tugas SET
                    status = ?
					WHERE id= ?;";
         
        $data = array(
			$n,
			$i
        );
         
        $sth = $this->_db->prepare($sql);
        return $sth->execute($data);
    }
	
	public function store()
	{
		$sql = "INSERT INTO tugas 
					(id, nama, tgl_deadline, status, pemilik)
				VALUES 
					(?, ?, ?, ?, ?);";
		 
		$data = array(
			$this->_id_tugas,
			$this->_taskname,
			$this->_tgl_deadline,
			$this->_status,
			$this->_pemilik
		);
		 
		$sth = $this->_db->prepare($sql);
		return $sth->execute($data);
	}
}