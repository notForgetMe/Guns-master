/**
 * 用户列表
 */
var User = {
    id: "userTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
User.initColumn = function () {
     var columns = [
        {field: 'selectItem', radio: true},
        {title: 'id', field: 'id', visible: false, align: 'center', valign: 'middle'},
        {title: '账号', field: 'account', align: 'center', valign: 'middle', sortable: true},
        {title: '姓名', field: 'name', align: 'center', valign: 'middle', sortable: true},
        {title: '昵称', field: 'nickName', align: 'center', valign: 'middle', sortable: true},
        {title: '余额', field: 'balance', align: 'center', valign: 'middle', sortable: true},
        {title: '注册时间', field: 'createtime', align: 'center', valign: 'middle', sortable: true},
        {title: '状态', field: 'is_login', align: 'center', valign: 'middle', sortable: true, formatter:function(value,row,index){ 
        	if(value==1){
        		return "在线";
        	}else{
        		return "离线";
        	}
        }},
        {title: '用户类型', field: 'roleid', align: 'center', valign: 'middle', sortable: true, formatter:function(value,row,index){ 
        	if(value==1){
        		return "普通用户";
        	}if(value==2){
        		return "代理用户";
        	}if(value==3){
        		return "运营人员";
        	}if(value==4){
        		return "主管理员";
        	}else{
        		return "虚拟用户";
        	}
        }},
        {title: '登录时间', field: 'login_time', align: 'center', valign: 'middle', sortable: true}]
    return columns;
};


/**
 * 检查是否选中
 */
User.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if (selected.length == 0) {
        Feng.info("请先选中表格中的某一记录！");
        return false;
    } else {
        User.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加管理员
 */
User.openAddMgr = function () {
    var index = layer.open({
        type: 2,
        title: '添加用户',
        area: ['500px', '300px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/mgr/user_add'
    });
    this.layerIndex = index;
};

/**
 * 点击修改按钮时
 */
User.openChangeUser = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '编辑用户',
            area: ['800px', '300px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/mgr/user_edit/' + this.seItem.id
        });
        this.layerIndex = index;
    }
};


/**
 * 搜索角色
 */
User.search = function () {
    var queryData = {};
    queryData['name'] = $("#userName").val();
    queryData['beginTime']=$("#beginTime").val();
    queryData['endTime']=$("#endTime").val();
    User.table.refresh({query: queryData});
}

$(function () {
    var defaultColunms = User.initColumn();
    var table = new BSTable(User.id, "/mgr/list", defaultColunms);
    table.setPaginationType("client");
    table.init();
    User.table = table;
});