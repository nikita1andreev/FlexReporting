package com.nnn.flexrep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;


@SuppressWarnings("deprecation")
public class ReportGenerator //extends AbstractReport 
{
	private String P_Version = "4.5";   
	private static final Logger log = Logger.getLogger(ReportGenerator.class);
	private static final int MAX_HEIGHT = 200;
	private Long DEFAULT_INTERVAL = new Long(7);
	private Long maxCount = (long) 0;
	private Collection<Long> openIssueCounts = new ArrayList<Long>();
	private Collection<Date> dates = new ArrayList<Date>();
	private String BASEURL = "";
	String TypeFilter[];


	List<HashMap> getData()  {
		List <HashMap> a = new ArrayList(); 
		a.add(newHashMap("a,b,c,d,f,url","a1","b1","c1",10l,"first","url1"));
		a.add(newHashMap("a,b,c,d,f,url","a1","b2","c1",-20l,"second","url2"));
		a.add(newHashMap("a,b,c,d,f,url","a1","b2","c2",30l,"fhird","url3"));		
		return a;
	}
	
	public static HashMap<String, Object> newHashMap(Object ... args) {				
		HashMap<String, Object> hm = new HashMap<String, Object>();
		StringTokenizer st = new StringTokenizer((String) args[0], ",");
		int i = 1;
		while (st.hasMoreElements()) {
			hm.put((String) st.nextElement(), args[i]);
			i++;
		}
		return hm;
	}
	

	public class Node implements Cloneable {
		public ArrayList<Node> nodes;
		public LinkedHashMap fields;
		public HashMap additional;
		public Node parent;

		public void add(Node n) {
			nodes.add(n);
			n.parent = this;
		}

		public Node()  {
			nodes = new ArrayList<ReportGenerator.Node>();
			fields = new LinkedHashMap();
			additional = new HashMap();
		}
		public Node(Node a)  {
			nodes = (ArrayList<Node>) a.nodes.clone();
			fields = (LinkedHashMap) a.fields.clone();
			additional = (HashMap) a.additional.clone();
		}

		public Node next() {
			if (nodes.size() > 0)
				return nodes.get(0);
			Node par = parent;
			Node curr = this;
			while (par != null) {
				int i = par.nodes.lastIndexOf(curr);
				if (i != -1) {
					if (i < par.nodes.size() - 1) {
						return par.nodes.get(i + 1);
					} else {
						curr = par;
						par = par.parent;
					}
				}
				;
			}
			return null;
		}

		public int getlevel() {
			int l = 0;
			Node p = this.parent;
			while (p != null) {
				l = l + 1;
				p = p.parent;
			}
			return l;
		}
		public Node clone() throws CloneNotSupportedException{
			return new Node(this);
			}
	}

	private void add_val(HashSet val, Object cur_val) {
		boolean found = false;
		for (Object e : val) { //Идем по элементам коллекции в которую надо добавить значение
			if (e == null) { // уже есть пустое
				if (cur_val == null) { // а мы и хотели добавить пустое
					found = true;
					break;
				} else { //мы хотим добавить не пустое - смотрим дальше
					continue;
					// found = true;
					// break;
				}
			}
			else if (e.equals(cur_val)) { //уже есть такое же
				found = true;
				break;
			}
			else if (getStringName(e).equals(getStringName(cur_val))) { //или с таким же строковым представлением 
				found = true;
				break;
			}
		}
		if (!found)
			val.add(cur_val);
	}

	public void generate(Node node, 
			ArrayList<HashMap> t, ArrayList groupsFeildsList, int level) {
		if (groupsFeildsList.size() == 0)
			return;
		HashSet val = new HashSet();
		for (HashMap ht : t) {// По всем запросам
			if (check(ht, node)) { //Если прошла проверка на фильтр узла
				Object cur_val = ht.get(groupsFeildsList.get(level));
				if (cur_val == null) //Пустое значение добавляем сразу
					add_val(val, cur_val);
				else if (Iterable.class.isAssignableFrom(cur_val.getClass())) {
					val.add(null); //Если это коллекция добавляем и пустое
					for (Object it_val : (Iterable) cur_val)
						add_val(val, it_val); //И каждое из коллекции
				} else {
					add_val(val, cur_val);
				}
			};			
		}
		ArrayList sortedList = new ArrayList(val);
		if (groupsFeildsList.get(level) == null)
			return;
			
	
	
		Collections.sort(sortedList, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return getStringName(o1).compareTo(getStringName(o2));
			}
		});		
		if (level < groupsFeildsList.size())
			for (Object o : sortedList) {
				LinkedHashMap newFilter = (LinkedHashMap) node.fields.clone();
				newFilter.put(groupsFeildsList.get(level), o);
				Node newNode = new Node();
				newNode.fields = newFilter;
				node.add(newNode);
				if (level + 1 < groupsFeildsList.size())
					generate(// html,
							newNode, t, groupsFeildsList, level + 1);
			}
	}

	
	public static String getStringName(Object o) {
		if (o == null)
			return "---";
		if (o.getClass() == String.class)
			return (String) o;
		if (Iterable.class.isAssignableFrom(o.getClass())) {
			String str = "";
			for (Object e : (Iterable) o) {
					str = str + ((Object) e).toString() + " ";
			}
			if (str.equals(""))
				return "---";
			return str.trim();
		}
		return o.toString();
	}

	public boolean check(HashMap ht, Node x) {
		boolean right = true; //по умолчанию проверка пройдена		
		for (Object fe : x.fields.keySet()) { // по всем полям фильтра
			Object val = ht.get(fe);	//то что в строке
			Object val1 = x.fields.get(fe); //то что в фильтре
			if (val == null && val1 == null) //если оба пустые идем к следующему полю фильтра
				continue;
			if (val == null && val1 != null) { // фильтр не пустой а поле пустое
				right = false;
				break;
			}			
			if (Iterable.class.isAssignableFrom(val.getClass())) {	//это можно перебирать 
				if (Iterables.isEmpty(((Iterable) val)) && val1 == null) //коллекция пустая но и поле фильтра пусто
					continue;
				boolean found = false; //по умолчанию не нашли
				for (Object el : (Iterable) val) //перебираем
					if (el.equals(val1)||getStringName(el).equals(getStringName(val1))) { // нашли
						found = true;
						break;
					};
				if (!found) {//не нашли
					right = false;
					break;
				}
			} 
			 else if (!(val.equals(val1)||getStringName(val).equals(getStringName(val1)))) {
				right = false;
				break;
			}
		}
		return right;
	}

	private Object minmax(Object a,Object b, String f){
		if (a==null) return b;
		if (b==null) return a;
		if (Comparable.class.isAssignableFrom(a.getClass()))
		{
			if(((Comparable)a).compareTo(b)*(f.equals("max")?1:-1)>0)
				return a; 
			else 
				return b;
		}
		return f;				
	}
	
	@SuppressWarnings("unchecked")
	public HashMap compute(Node x, Node y, ArrayList<HashMap> t,ArrayList<HashMap> computeFeildsList) {
		HashMap hm = new HashMap();
		HashMap t_hm = new HashMap();

		Long count = (long) 0;
		Long t_count = (long) 0;
		if (x.nodes.size() > 0 || y.nodes.size() > 0) {
			ArrayList<Node> anx, any;
			if (x.nodes.size() > 0) {
				anx = x.nodes;
			} else {
				anx = new ArrayList<Node>();
				anx.add(x);
			}
			if (y.nodes.size() > 0) {
				any = y.nodes;
			} else {
				any = new ArrayList<Node>();
				any.add(y);
			}
			HashSet t_root_issues = new HashSet();
			for (Node nx : anx) {
				for (Node ny : any) {
					HashMap r = compute(nx, ny, t, computeFeildsList);
					for(HashMap e:computeFeildsList) {
						if (e.get("function").equals("sum")||e.get("function").equals("avg")){
							t_hm.put(e.get("field"), (((t_hm.get(e.get("field"))==null)?new Long(0):((Long)t_hm.get(e.get("field")))))+(Long)r.get(e.get("field")));
						}
						else if (e.get("function").equals("min")||e.get("function").equals("max")){
							t_hm.put(e.get("field"), minmax(t_hm.get(e.get("field")),r.get(e.get("field")),(String)e.get("function"))); 
						}
						
					}
					count = count + 1;
				}
			}
		} else {
			for (HashMap row : t) {
				if (check(row, x) && check(row, y)) {
					if (row.get("Computed") != null) {
						t_count = (Long) ((HashMap) row.get("Computed")).get("Count");
					} else {						
						for (HashMap e : computeFeildsList) {
							if (e.get("function").equals("sum") || e.get("function").equals("avg")) {
								t_hm.put(e.get("field"), (((t_hm.get(e.get("field")) == null ? new Long(0)
										: ((Long) t_hm.get(e.get("field")))) + (Long) row.get(e.get("field")))));
							} else if (e.get("function").equals("min")||e.get("function").equals("max")) {
								t_hm.put(e.get("field"), minmax(t_hm.get(e.get("field")),row.get(e.get("field")),(String)e.get("function")));
							} 
						}
						t_count = (long) 1;
					}
					count = count + t_count;
				}
				;
			}
		}
		hm.put("Count", count);
		for(HashMap e:computeFeildsList)
			if (e.get("function").equals("sum") || e.get("function").equals("avg")) {
				hm.put(e.get("field"), (Long)((t_hm.get(e.get("field"))==null)?(new Long(0)):t_hm.get(e.get("field")))
						/ ((e.get("function").equals("avg")&&count!=0)?count:1)
						);
			} else 
			hm.put(e.get("field"), t_hm.get(e.get("field")));
		
		return hm;
	}


	public String generateReportHtml(
			) throws Exception {

		Set<Object> allFields = new HashSet();
		ArrayList<Object> groupsFeildsList = new ArrayList<Object>();
		ArrayList<Object> detailsFeildsList = new ArrayList<Object>();
		ArrayList<HashMap> computeFeildsList = new ArrayList<HashMap>();

		groupsFeildsList.add("a");
		groupsFeildsList.add("b");
		groupsFeildsList.add("url");
		detailsFeildsList.add("c");
		computeFeildsList.add(newHashMap("field,function","d","max"));

		ArrayList<HashMap> t;
		ArrayList<HashMap> t_base = (ArrayList<HashMap>) getData();

		boolean ShowAll = false;
		
		String out = ""; 

		t = (ArrayList<HashMap>) t_base.clone();

		Node vert = new Node();
		Node hor = new Node();

		generate(vert, t, groupsFeildsList, 0);
		generate(hor, t, detailsFeildsList, 0);

		String html = "<br><button onclick=\"SwapAll(false);\">Roll up</button></a><button onclick=\"SwapAll(true);\">Unroll</button></a>"
				+ "<table class=\"treetable\" style=\"width: 100%; border: 0; background-color: lightgrey\">";// id=\"report\"
		Node x =  vert.clone();
		Node y = hor.clone();
		Node hor_empty = hor.clone();
		hor_empty.nodes.clear();
		
		String spaceInsert = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";  
		
		String widthHTML = " width=\"150\""; 
		String styleText = "";// "style=\"background-color: #eee; padding:
								// 1px\"";
		html = html + "<thead><tr " + styleText + ">";
		html = html + "<td align=\"center\" width=\"300\"><b></b></td>"; 

		while (y != null) {
			if (y.fields.size() != 0) {
				html = html + "<td align=\"center\"" + widthHTML + "><b>"
						+ (y.fields.size() > 0
								? getStringName(y.fields.get(y.fields.keySet().toArray()[y.fields.keySet().size() - 1]))
								: "")
						+ "</b></td>";
			}
			y = y.next();

		}
		html = html + "</tr></thead>";
		while (x != null) {
			if (x.fields.size() != 0) {

				if (x.getlevel() >= groupsFeildsList.size()) {
					String URL = BASEURL
							+ "/browse/";
					String ADD = "";
					URL = (String) x.fields.get("url");//"";

					html = html + "<tr class=\"lev" + x.getlevel() + "\" " + styleText + ">";
					html = html + "<td align=\"left\">" + StringUtils.repeat(spaceInsert, x.getlevel()-1) + "<a href=" + URL + " target=\"_blank\">"  
							+ (x.fields.size() > 0
									? getStringName(
											x.fields.get(x.fields.keySet().toArray()[x.fields.keySet().size() - 1]))  
									: "")
							+ ADD + "</a></td>";

				}
				else {
					html = html + "<tr class=\"lev" + x.getlevel() + "\" " + styleText + ">";
					html = html + "<td align=\"left\">" + "<label><input type=\"checkbox\"><a onclick=\"sh(this)\">"+ StringUtils.repeat(spaceInsert, x.getlevel()-1)
							+ (x.fields.size() > 0
									? getStringName(
											x.fields.get(x.fields.keySet().toArray()[x.fields.keySet().size() - 1]))
									: "")
							+ "</a></label></td>";
				}
				HashMap compVals = compute(x, hor_empty, t, computeFeildsList);


				y = hor;
				while (y != null) {
					HashMap compVals1 = compute(x, y, t,computeFeildsList);
					if (y.fields.size() != 0)
						html = html + "<td align=\"center\">"+(x.getlevel() >= groupsFeildsList.size()?"":"<b>") //+ StringUtils.repeat(spaceInsert, x.getlevel()-1)
								+getCompView(compVals1,computeFeildsList)//+ (!compVals1.get("Procent").equals("") ? (compVals1.get("Procent") ) : "")// ht.get(detailsFeildsList.get(i)).toString()+
								+(x.getlevel() >= groupsFeildsList.size()?"":"</b>")+"</td>";
					y = y.next();
				}
				html = html + "</tr>";
			}
			;
			x = x.next();
		}
		html = html + "<tr class = \"lev1\"" + styleText + ">";
		html = html + "<td align=\"left\"><b>Grand total</b></td>";
		HashMap compVals = compute(vert, hor_empty, t,computeFeildsList);

		y = hor;
		while (y != null) {
			HashMap compVals1 = compute(vert, y, t,computeFeildsList);
			if (y.fields.size() != 0)
				html = html + "<td align=\"center\"><b>"
						+getCompView(compVals1,computeFeildsList)+//+ (!compVals1.get("Procent").equals("") ? (compVals1.get("Procent") ) : "") + // ht.get(detailsFeildsList.get(i)).toString()+
						"</b></td>";
			y = y.next();
		}
		html = html + "</tr>";
		html = html + "</table>";
		html = html + out;////////////////////////////////////////////////////////////////////////////

		return html;
	}
	
	private String getCompView(HashMap compVals1,List <HashMap> computeFeildsList){
		String res = "";
		for(HashMap e:computeFeildsList){
			res = res + (res.equals("")?"":",") +(compVals1.get(e.get("field"))==null?"":compVals1.get(e.get("field")));
		}
		return res;		
	}	
	}

         