// 全局变量
let currentPage = {
    circuitBoard: 1,
    semiProduct: 1,
    ledBoardPlugin: 1
};

let currentEditingId = {
    circuitBoard: null,
    semiProduct: null,
    ledBoardPlugin: null
};

// API基础URL
const API_BASE_URL = 'http://localhost:8083';

// 显示不同的管理模块
function showSection(section) {
    // 隐藏所有section
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.style.display = 'none';
    });
    
    // 显示选中的section
    document.getElementById(section + '-section').style.display = 'block';
    
    // 更新导航栏激活状态
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // 加载对应的数据
    switch(section) {
        case 'tree':
            loadTreeData();
            break;
        case 'circuit-board':
            loadCircuitBoards();
            break;
        case 'semi-product':
            loadSemiProducts();
            loadCircuitBoardOptions();
            break;
        case 'led-board-plugin':
            loadLedBoardPlugins();
            loadSemiProductOptions();
            break;
    }
}

// 显示加载动画
function showLoading() {
    document.querySelector('.loading-spinner').style.display = 'block';
}

// 隐藏加载动画
function hideLoading() {
    document.querySelector('.loading-spinner').style.display = 'none';
}

// 通用API调用函数
async function apiCall(url, method = 'GET', data = null) {
    try {
        showLoading();
        console.log('API调用:', method, url, data);
        
        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };
        
        if (data) {
            options.body = JSON.stringify(data);
        }
        
        const response = await fetch(url, options);
        const result = await response.json();
        
        console.log('API响应:', result);
        
        if (result.code === 200) {
            return result.data;
        } else {
            throw new Error(result.message || '操作失败');
        }
    } catch (error) {
        console.error('API调用错误:', error);
        alert('操作失败: ' + error.message);
        throw error;
    } finally {
        hideLoading();
    }
}

// 树形结构管理功能
let treeData = [];
let expandedNodes = new Set();

// 日期格式化函数
function formatDate(dateString) {
    if (!dateString) return '无';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

async function loadTreeData() {
    try {
        showLoading();
        
        // 加载所有线路板
        const circuitBoards = await apiCall(`${API_BASE_URL}/circuit-board/list?page=1&size=100`);
        
        // 构建树形数据
        treeData = [];
        
        if (circuitBoards && circuitBoards.length > 0) {
            for (const board of circuitBoards) {
                const boardNode = {
                    id: `board-${board.id}`,
                    type: 'circuitBoard',
                    data: board,
                    children: []
                };
                
                // 加载该线路板下的半成品
                const semiProducts = await apiCall(`${API_BASE_URL}/semi-product/list-by-circuit-board/${board.id}`);
                
                if (semiProducts && semiProducts.length > 0) {
                    for (const semiProduct of semiProducts) {
                        const semiProductNode = {
                            id: `semi-${semiProduct.id}`,
                            type: 'semiProduct',
                            data: semiProduct,
                            children: []
                        };
                        
                        // 加载该半成品下的灯板插件
                        const ledBoardPlugins = await apiCall(`${API_BASE_URL}/led-board-plugin-semi-product/list-by-semi-product/${semiProduct.id}`);
                        
                        if (ledBoardPlugins && ledBoardPlugins.length > 0) {
                            semiProductNode.children = ledBoardPlugins.map(plugin => ({
                                id: `plugin-${plugin.id}`,
                                type: 'ledBoardPlugin',
                                data: plugin,
                                children: []
                            }));
                        }
                        
                        boardNode.children.push(semiProductNode);
                    }
                }
                
                treeData.push(boardNode);
            }
        }
        
        displayTreeData();
        updateTreeStats();
        
    } catch (error) {
        console.error('加载树形数据失败:', error);
        document.getElementById('productTree').innerHTML = '<div class="text-center text-muted">加载失败，请稍后重试</div>';
    }
}

function displayTreeData(searchTerm = '', statusFilter = '') {
    const treeContainer = document.getElementById('productTree');
    
    if (!treeData || treeData.length === 0) {
        treeContainer.innerHTML = '<div class="text-center text-muted">暂无数据</div>';
        return;
    }
    
    // 过滤数据
    const filteredData = filterTreeData(treeData, searchTerm, statusFilter);
    
    if (filteredData.length === 0) {
        treeContainer.innerHTML = '<div class="text-center text-muted">没有符合条件的数据</div>';
        return;
    }
    
    treeContainer.innerHTML = '';
    
    filteredData.forEach(node => {
        const nodeElement = createTreeNode(node, 0);
        treeContainer.appendChild(nodeElement);
    });
}

function filterTreeData(data, searchTerm, statusFilter) {
    if (!searchTerm && !statusFilter) {
        return data;
    }
    
    return data.filter(node => {
        let matchesSearch = !searchTerm;
        
        if (searchTerm) {
            const term = searchTerm.toLowerCase();
            // 根据节点类型进行不同的搜索
            switch (node.type) {
                case 'circuitBoard':
                    matchesSearch = 
                        node.data.boardCode?.toLowerCase().includes(term) ||
                        node.data.boardName?.toLowerCase().includes(term) ||
                        node.data.spec?.toLowerCase().includes(term);
                    break;
                case 'semiProduct':
                    matchesSearch = 
                        node.data.semiProductCode?.toLowerCase().includes(term) ||
                        node.data.semiProductName?.toLowerCase().includes(term) ||
                        node.data.circuitBoardName?.toLowerCase().includes(term);
                    break;
                case 'ledBoardPlugin':
                    matchesSearch = 
                        node.data.ledBoardPluginCode?.toLowerCase().includes(term) ||
                        node.data.ledBoardPluginName?.toLowerCase().includes(term) ||
                        node.data.semiProductName?.toLowerCase().includes(term);
                    break;
            }
        }
        
        const matchesStatus = !statusFilter || node.data.status == statusFilter;
        
        if (matchesSearch && matchesStatus) {
            return true;
        }
        
        // 递归过滤子节点
        if (node.children && node.children.length > 0) {
            node.children = filterTreeData(node.children, searchTerm, statusFilter);
            return node.children.length > 0;
        }
        
        return false;
    });
}

function createTreeNode(node, level) {
    const nodeDiv = document.createElement('div');
    nodeDiv.className = `tree-node ${node.type}-node`;
    nodeDiv.setAttribute('data-id', node.id);
    
    const itemDiv = document.createElement('div');
    itemDiv.className = 'tree-item';
    
    // 展开/收起按钮
    const toggleBtn = document.createElement('button');
    toggleBtn.className = 'tree-toggle';
    toggleBtn.innerHTML = '▶';
    
    if (node.children && node.children.length > 0) {
        toggleBtn.onclick = () => toggleTreeNode(node.id);
        if (expandedNodes.has(node.id)) {
            toggleBtn.classList.add('expanded');
        }
    } else {
        toggleBtn.style.visibility = 'hidden';
    }
    
    // 图标
    const icon = document.createElement('i');
    icon.className = `tree-icon bi ${getTreeNodeIcon(node.type)}`;
    
    // 内容
    const contentDiv = document.createElement('div');
    contentDiv.className = 'tree-content';
    
    const infoDiv = document.createElement('div');
    infoDiv.className = 'tree-info';
    
    const titleDiv = document.createElement('div');
    titleDiv.className = 'tree-title';
    titleDiv.textContent = getTreeNodeTitle(node);
    
    const subtitleDiv = document.createElement('div');
    subtitleDiv.className = 'tree-subtitle';
    subtitleDiv.textContent = getTreeNodeSubtitle(node);
    
    infoDiv.appendChild(titleDiv);
    infoDiv.appendChild(subtitleDiv);
    
    const metaDiv = document.createElement('div');
    metaDiv.className = 'tree-meta';
    
    // 状态标签
    const statusSpan = document.createElement('span');
    statusSpan.className = `tree-status ${node.data.status === 1 ? 'bg-success text-white' : 'bg-secondary text-white'}`;
    statusSpan.textContent = node.data.status === 1 ? '启用' : '禁用';
    
    // 操作按钮
    const actionsDiv = document.createElement('div');
    actionsDiv.className = 'tree-actions';
    
    const editBtn = document.createElement('button');
    editBtn.className = 'tree-action-btn btn-outline-primary';
    editBtn.innerHTML = '<i class="bi bi-pencil"></i>';
    editBtn.onclick = () => editTreeNode(node);
    
    const addBtn = document.createElement('button');
    addBtn.className = 'tree-action-btn btn-outline-success';
    addBtn.innerHTML = '<i class="bi bi-plus"></i>';
    addBtn.onclick = () => addTreeNodeChild(node);
    
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'tree-action-btn btn-outline-danger';
    deleteBtn.innerHTML = '<i class="bi bi-trash"></i>';
    deleteBtn.onclick = () => deleteTreeNode(node);
    
    actionsDiv.appendChild(editBtn);
    if (node.type !== 'ledBoardPlugin') {
        actionsDiv.appendChild(addBtn);
    }
    actionsDiv.appendChild(deleteBtn);
    
    metaDiv.appendChild(statusSpan);
    metaDiv.appendChild(actionsDiv);
    
    contentDiv.appendChild(infoDiv);
    contentDiv.appendChild(metaDiv);
    
    itemDiv.appendChild(toggleBtn);
    itemDiv.appendChild(icon);
    itemDiv.appendChild(contentDiv);
    
    nodeDiv.appendChild(itemDiv);
    
    // 子节点
    if (node.children && node.children.length > 0 && expandedNodes.has(node.id)) {
        const childrenDiv = document.createElement('div');
        childrenDiv.className = `tree-children tree-level-${level} expanded`;
        
        node.children.forEach(child => {
            const childElement = createTreeNode(child, level + 1);
            childrenDiv.appendChild(childElement);
        });
        
        nodeDiv.appendChild(childrenDiv);
    }
    
    return nodeDiv;
}

function getTreeNodeIcon(type) {
    switch (type) {
        case 'circuitBoard':
            return 'bi-motherboard';
        case 'semiProduct':
            return 'bi-box';
        case 'ledBoardPlugin':
            return 'bi-lightbulb';
        default:
            return 'bi-circle';
    }
}

function getTreeNodeTitle(node) {
    switch (node.type) {
        case 'circuitBoard':
            return `${node.data.boardCode} - ${node.data.boardName}`;
        case 'semiProduct':
            return `${node.data.semiProductCode} - ${node.data.semiProductName}`;
        case 'ledBoardPlugin':
            return `${node.data.ledBoardPluginCode} - ${node.data.ledBoardPluginName}`;
        default:
            return '未知';
    }
}

function getTreeNodeSubtitle(node) {
    switch (node.type) {
        case 'circuitBoard':
            return `规格: ${node.data.spec || '无'} | 创建时间: ${formatDate(node.data.createTime)}`;
        case 'semiProduct':
            return `所属线路板: ${node.data.circuitBoardName || '未知'} | 创建时间: ${formatDate(node.data.createTime)}`;
        case 'ledBoardPlugin':
            return `所属半成品: ${node.data.semiProductName || '未知'} | 创建时间: ${formatDate(node.data.createTime)}`;
        default:
            return '';
    }
}

function toggleTreeNode(nodeId) {
    if (expandedNodes.has(nodeId)) {
        expandedNodes.delete(nodeId);
    } else {
        expandedNodes.add(nodeId);
    }
    
    // 重新显示树，保持当前搜索和过滤状态
    const searchTerm = document.getElementById('treeSearchInput').value;
    const statusFilter = document.getElementById('treeStatusFilter').value;
    displayTreeData(searchTerm, statusFilter);
}

function expandAllTreeNodes() {
    // 递归添加所有节点到展开集合
    function addAllNodes(nodes) {
        nodes.forEach(node => {
            if (node.children && node.children.length > 0) {
                expandedNodes.add(node.id);
                addAllNodes(node.children);
            }
        });
    }
    
    addAllNodes(treeData);
    displayTreeData();
}

function collapseAllTreeNodes() {
    expandedNodes.clear();
    displayTreeData();
}

function searchTreeData() {
    const searchTerm = document.getElementById('treeSearchInput').value;
    const statusFilter = document.getElementById('treeStatusFilter').value;
    displayTreeData(searchTerm, statusFilter);
}

function updateTreeStats() {
    const stats = calculateTreeStats(treeData);
    
    const statsHtml = `
        <div class="tree-stats">
            <div class="tree-stat-item">
                <h5>${stats.circuitBoardCount}</h5>
                <p>线路板</p>
            </div>
            <div class="tree-stat-item">
                <h5>${stats.semiProductCount}</h5>
                <p>半成品</p>
            </div>
            <div class="tree-stat-item">
                <h5>${stats.ledBoardPluginCount}</h5>
                <p>灯板插件</p>
            </div>
            <div class="tree-stat-item">
                <h5>${stats.totalCount}</h5>
                <p>总计</p>
            </div>
        </div>
    `;
    
    const treeSection = document.getElementById('tree-section');
    const existingStats = treeSection.querySelector('.tree-stats');
    if (existingStats) {
        existingStats.remove();
    }
    
    treeSection.insertAdjacentHTML('afterbegin', statsHtml);
}

function calculateTreeStats(data) {
    let stats = { circuitBoardCount: 0, semiProductCount: 0, ledBoardPluginCount: 0, totalCount: 0 };
    
    function countNodes(nodes) {
        nodes.forEach(node => {
            switch (node.type) {
                case 'circuitBoard':
                    stats.circuitBoardCount++;
                    break;
                case 'semiProduct':
                    stats.semiProductCount++;
                    break;
                case 'ledBoardPlugin':
                    stats.ledBoardPluginCount++;
                    break;
            }
            stats.totalCount++;
            
            if (node.children && node.children.length > 0) {
                countNodes(node.children);
            }
        });
    }
    
    countNodes(data);
    return stats;
}

function editTreeNode(node) {
    switch (node.type) {
        case 'circuitBoard':
            editCircuitBoard(node.data.id);
            break;
        case 'semiProduct':
            editSemiProduct(node.data.id);
            break;
        case 'ledBoardPlugin':
            editLedBoardPlugin(node.data.id);
            break;
    }
}

function addTreeNodeChild(node) {
    switch (node.type) {
        case 'circuitBoard':
            currentEditingId.circuitBoard = null;
            document.getElementById('semiProductModalTitle').textContent = '新增半成品';
            document.getElementById('semiProductForm').reset();
            document.getElementById('modalSemiProductCircuitBoard').value = node.data.id;
            new bootstrap.Modal(document.getElementById('semiProductModal')).show();
            break;
        case 'semiProduct':
            currentEditingId.ledBoardPlugin = null;
            document.getElementById('ledBoardPluginModalTitle').textContent = '新增灯板插件';
            document.getElementById('ledBoardPluginForm').reset();
            document.getElementById('modalLedBoardPluginSemiProduct').value = node.data.id;
            new bootstrap.Modal(document.getElementById('ledBoardPluginModal')).show();
            break;
        case 'ledBoardPlugin':
            alert('灯板插件是最底层，无法添加子节点');
            break;
    }
}

function deleteTreeNode(node) {
    if (!confirm(`确定要删除 ${getTreeNodeTitle(node)} 吗？`)) return;
    
    switch (node.type) {
        case 'circuitBoard':
            deleteCircuitBoard(node.data.id);
            break;
        case 'semiProduct':
            deleteSemiProduct(node.data.id);
            break;
        case 'ledBoardPlugin':
            deleteLedBoardPlugin(node.data.id);
            break;
    }
}

// 线路板管理功能
async function loadCircuitBoards(page = 1) {
    try {
        const boardCode = document.getElementById('circuitBoardCode').value;
        const boardName = document.getElementById('circuitBoardName').value;
        const status = document.getElementById('circuitBoardStatus').value;
        
        let url = `${API_BASE_URL}/circuit-board/list?page=${page}&size=10`;
        if (boardCode) url += `&boardCode=${boardCode}`;
        if (boardName) url += `&boardName=${boardName}`;
        if (status) url += `&status=${status}`;
        
        const data = await apiCall(url);
        displayCircuitBoards(data);
        currentPage.circuitBoard = page;
    } catch (error) {
        console.error('加载线路板失败:', error);
    }
}

function displayCircuitBoards(data) {
    const tbody = document.getElementById('circuitBoardTableBody');
    tbody.innerHTML = '';
    
    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">暂无数据</td></tr>';
        return;
    }
    
    data.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.id}</td>
            <td>${item.boardCode}</td>
            <td>${item.boardName}</td>
            <td>${item.spec || '-'}</td>
            <td>
                <span class="status-badge ${item.status === 1 ? 'bg-success' : 'bg-secondary'}">
                    ${item.status === 1 ? '启用' : '禁用'}
                </span>
            </td>
            <td>${formatDate(item.createTime)}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editCircuitBoard(${item.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteCircuitBoard(${item.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function searchCircuitBoards() {
    loadCircuitBoards(1);
}

function showAddCircuitBoardModal() {
    currentEditingId.circuitBoard = null;
    document.getElementById('circuitBoardModalTitle').textContent = '新增线路板';
    document.getElementById('circuitBoardForm').reset();
    new bootstrap.Modal(document.getElementById('circuitBoardModal')).show();
}

async function editCircuitBoard(id) {
    try {
        const data = await apiCall(`${API_BASE_URL}/circuit-board/get/${id}`);
        currentEditingId.circuitBoard = id;
        
        document.getElementById('circuitBoardModalTitle').textContent = '编辑线路板';
        document.getElementById('modalCircuitBoardCode').value = data.boardCode;
        document.getElementById('modalCircuitBoardName').value = data.boardName;
        document.getElementById('modalCircuitBoardSpec').value = data.spec || '';
        document.getElementById('modalCircuitBoardStatus').value = data.status;
        document.getElementById('modalCircuitBoardRemark').value = data.remark || '';
        
        new bootstrap.Modal(document.getElementById('circuitBoardModal')).show();
    } catch (error) {
        console.error('加载线路板详情失败:', error);
    }
}

async function saveCircuitBoard() {
    try {
        const formData = {
            boardCode: document.getElementById('modalCircuitBoardCode').value,
            boardName: document.getElementById('modalCircuitBoardName').value,
            spec: document.getElementById('modalCircuitBoardSpec').value,
            status: parseInt(document.getElementById('modalCircuitBoardStatus').value),
            remark: document.getElementById('modalCircuitBoardRemark').value
        };
        
        if (currentEditingId.circuitBoard) {
            formData.id = currentEditingId.circuitBoard;
            await apiCall(`${API_BASE_URL}/circuit-board/update`, 'PUT', formData);
        } else {
            await apiCall(`${API_BASE_URL}/circuit-board/add`, 'POST', formData);
        }
        
        bootstrap.Modal.getInstance(document.getElementById('circuitBoardModal')).hide();
        loadCircuitBoards(currentPage.circuitBoard);
        
        // 如果在树形结构页面，刷新树形数据
        if (document.getElementById('tree-section').style.display !== 'none') {
            loadTreeData();
        }
        
        alert('保存成功！');
    } catch (error) {
        console.error('保存线路板失败:', error);
    }
}

async function deleteCircuitBoard(id) {
    if (!confirm('确定要删除这条记录吗？')) return;
    
    try {
        await apiCall(`${API_BASE_URL}/circuit-board/delete/${id}`, 'DELETE');
        loadCircuitBoards(currentPage.circuitBoard);
        
        // 如果在树形结构页面，刷新树形数据
        if (document.getElementById('tree-section').style.display !== 'none') {
            loadTreeData();
        }
        
        alert('删除成功！');
    } catch (error) {
        console.error('删除线路板失败:', error);
    }
}

// 半成品管理功能
async function loadSemiProducts(page = 1) {
    try {
        const circuitBoardId = document.getElementById('semiProductCircuitBoard').value;
        const semiProductCode = document.getElementById('semiProductCode').value;
        const semiProductName = document.getElementById('semiProductName').value;
        
        let url = `${API_BASE_URL}/semi-product/list?page=${page}&size=10`;
        if (circuitBoardId) url += `&circuitBoardId=${circuitBoardId}`;
        if (semiProductCode) url += `&semiProductCode=${semiProductCode}`;
        if (semiProductName) url += `&semiProductName=${semiProductName}`;
        
        const data = await apiCall(url);
        displaySemiProducts(data);
        currentPage.semiProduct = page;
    } catch (error) {
        console.error('加载半成品失败:', error);
    }
}

function displaySemiProducts(data) {
    const tbody = document.getElementById('semiProductTableBody');
    tbody.innerHTML = '';
    
    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">暂无数据</td></tr>';
        return;
    }
    
    data.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.id}</td>
            <td>${item.semiProductCode}</td>
            <td>${item.semiProductName}</td>
            <td>${item.circuitBoardName || '-'}</td>
            <td>
                <span class="status-badge ${item.status === 1 ? 'bg-success' : 'bg-secondary'}">
                    ${item.status === 1 ? '启用' : '禁用'}
                </span>
            </td>
            <td>${formatDate(item.createTime)}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editSemiProduct(${item.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteSemiProduct(${item.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function loadCircuitBoardOptions() {
    try {
        const data = await apiCall(`${API_BASE_URL}/circuit-board/list?page=1&size=100`);
        const selects = [
            document.getElementById('semiProductCircuitBoard'),
            document.getElementById('modalSemiProductCircuitBoard')
        ];
        
        selects.forEach(select => {
            if (select) {
                select.innerHTML = '<option value="">选择线路板</option>';
                
                if (data && data.length > 0) {
                    data.forEach(item => {
                        const option = document.createElement('option');
                        option.value = item.id;
                        option.textContent = `${item.boardCode} - ${item.boardName}`;
                        select.appendChild(option);
                    });
                }
            }
        });
    } catch (error) {
        console.error('加载线路板选项失败:', error);
    }
}

function searchSemiProducts() {
    loadSemiProducts(1);
}

function showAddSemiProductModal() {
    currentEditingId.semiProduct = null;
    document.getElementById('semiProductModalTitle').textContent = '新增半成品';
    document.getElementById('semiProductForm').reset();
    new bootstrap.Modal(document.getElementById('semiProductModal')).show();
}

async function editSemiProduct(id) {
    try {
        const data = await apiCall(`${API_BASE_URL}/semi-product/get/${id}`);
        currentEditingId.semiProduct = id;
        
        document.getElementById('semiProductModalTitle').textContent = '编辑半成品';
        document.getElementById('modalSemiProductCode').value = data.semiProductCode;
        document.getElementById('modalSemiProductName').value = data.semiProductName;
        document.getElementById('modalSemiProductCircuitBoard').value = data.circuitBoardId;
        document.getElementById('modalSemiProductStatus').value = data.status;
        document.getElementById('modalSemiProductRemark').value = data.remark || '';
        
        new bootstrap.Modal(document.getElementById('semiProductModal')).show();
    } catch (error) {
        console.error('加载半成品详情失败:', error);
    }
}

async function saveSemiProduct() {
    try {
        const formData = {
            semiProductCode: document.getElementById('modalSemiProductCode').value,
            semiProductName: document.getElementById('modalSemiProductName').value,
            circuitBoardId: parseInt(document.getElementById('modalSemiProductCircuitBoard').value),
            status: parseInt(document.getElementById('modalSemiProductStatus').value),
            remark: document.getElementById('modalSemiProductRemark').value
        };
        
        if (currentEditingId.semiProduct) {
            formData.id = currentEditingId.semiProduct;
            await apiCall(`${API_BASE_URL}/semi-product/update`, 'PUT', formData);
        } else {
            await apiCall(`${API_BASE_URL}/semi-product/add`, 'POST', formData);
        }
        
        bootstrap.Modal.getInstance(document.getElementById('semiProductModal')).hide();
        loadSemiProducts(currentPage.semiProduct);
        
        // 如果在树形结构页面，刷新树形数据
        if (document.getElementById('tree-section').style.display !== 'none') {
            loadTreeData();
        }
        
        alert('保存成功！');
    } catch (error) {
        console.error('保存半成品失败:', error);
    }
}

async function deleteSemiProduct(id) {
    if (!confirm('确定要删除这条记录吗？')) return;
    
    try {
        await apiCall(`${API_BASE_URL}/semi-product/delete/${id}`, 'DELETE');
        loadSemiProducts(currentPage.semiProduct);
        
        // 如果在树形结构页面，刷新树形数据
        if (document.getElementById('tree-section').style.display !== 'none') {
            loadTreeData();
        }
        
        alert('删除成功！');
    } catch (error) {
        console.error('删除半成品失败:', error);
    }
}

// 灯板插件管理功能
async function loadLedBoardPlugins(page = 1) {
    try {
        const semiProductId = document.getElementById('ledBoardPluginSemiProduct').value;
        const ledBoardPluginCode = document.getElementById('ledBoardPluginCode').value;
        const ledBoardPluginName = document.getElementById('ledBoardPluginName').value;
        
        let url = `${API_BASE_URL}/led-board-plugin-semi-product/list?page=${page}&size=10`;
        if (semiProductId) url += `&semiProductId=${semiProductId}`;
        if (ledBoardPluginCode) url += `&ledBoardPluginCode=${ledBoardPluginCode}`;
        if (ledBoardPluginName) url += `&ledBoardPluginName=${ledBoardPluginName}`;
        
        const data = await apiCall(url);
        displayLedBoardPlugins(data);
        currentPage.ledBoardPlugin = page;
    } catch (error) {
        console.error('加载灯板插件失败:', error);
    }
}

function displayLedBoardPlugins(data) {
    const tbody = document.getElementById('ledBoardPluginTableBody');
    tbody.innerHTML = '';
    
    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">暂无数据</td></tr>';
        return;
    }
    
    data.forEach(item => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${item.id}</td>
            <td>${item.ledBoardPluginCode}</td>
            <td>${item.ledBoardPluginName}</td>
            <td>${item.semiProductName || '-'}</td>
            <td>
                <span class="status-badge ${item.status === 1 ? 'bg-success' : 'bg-secondary'}">
                    ${item.status === 1 ? '启用' : '禁用'}
                </span>
            </td>
            <td>${formatDate(item.createTime)}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editLedBoardPlugin(${item.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteLedBoardPlugin(${item.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function loadSemiProductOptions() {
    try {
        const data = await apiCall(`${API_BASE_URL}/semi-product/list?page=1&size=100`);
        const selects = [
            document.getElementById('ledBoardPluginSemiProduct'),
            document.getElementById('modalLedBoardPluginSemiProduct')
        ];
        
        selects.forEach(select => {
            if (select) {
                select.innerHTML = '<option value="">选择半成品</option>';
                
                if (data && data.length > 0) {
                    data.forEach(item => {
                        const option = document.createElement('option');
                        option.value = item.id;
                        option.textContent = `${item.semiProductCode} - ${item.semiProductName}`;
                        select.appendChild(option);
                    });
                }
            }
        });
    } catch (error) {
        console.error('加载半成品选项失败:', error);
    }
}

function searchLedBoardPlugins() {
    loadLedBoardPlugins(1);
}

function showAddLedBoardPluginModal() {
    currentEditingId.ledBoardPlugin = null;
    document.getElementById('ledBoardPluginModalTitle').textContent = '新增灯板插件';
    document.getElementById('ledBoardPluginForm').reset();
    new bootstrap.Modal(document.getElementById('ledBoardPluginModal')).show();
}

async function editLedBoardPlugin(id) {
    try {
        const data = await apiCall(`${API_BASE_URL}/led-board-plugin-semi-product/get/${id}`);
        currentEditingId.ledBoardPlugin = id;
        
        document.getElementById('ledBoardPluginModalTitle').textContent = '编辑灯板插件';
        document.getElementById('modalLedBoardPluginCode').value = data.ledBoardPluginCode;
        document.getElementById('modalLedBoardPluginName').value = data.ledBoardPluginName;
        document.getElementById('modalLedBoardPluginSemiProduct').value = data.semiProductId;
        document.getElementById('modalLedBoardPluginStatus').value = data.status;
        document.getElementById('modalLedBoardPluginRemark').value = data.remark || '';
        
        new bootstrap.Modal(document.getElementById('ledBoardPluginModal')).show();
    } catch (error) {
        console.error('加载灯板插件详情失败:', error);
    }
}

async function saveLedBoardPlugin() {
    try {
        const formData = {
            ledBoardPluginCode: document.getElementById('modalLedBoardPluginCode').value,
            ledBoardPluginName: document.getElementById('modalLedBoardPluginName').value,
            semiProductId: parseInt(document.getElementById('modalLedBoardPluginSemiProduct').value),
            status: parseInt(document.getElementById('modalLedBoardPluginStatus').value),
            remark: document.getElementById('modalLedBoardPluginRemark').value
        };
        
        if (currentEditingId.ledBoardPlugin) {
            formData.id = currentEditingId.ledBoardPlugin;
            await apiCall(`${API_BASE_URL}/led-board-plugin-semi-product/update`, 'PUT', formData);
        } else {
            await apiCall(`${API_BASE_URL}/led-board-plugin-semi-product/add`, 'POST', formData);
        }
        
        bootstrap.Modal.getInstance(document.getElementById('ledBoardPluginModal')).hide();
        loadLedBoardPlugins(currentPage.ledBoardPlugin);
        
        // 如果在树形结构页面，刷新树形数据
        if (document.getElementById('tree-section').style.display !== 'none') {
            loadTreeData();
        }
        
        alert('保存成功！');
    } catch (error) {
        console.error('保存灯板插件失败:', error);
    }
}

async function deleteLedBoardPlugin(id) {
    if (!confirm('确定要删除这条记录吗？')) return;
    
    try {
        await apiCall(`${API_BASE_URL}/led-board-plugin-semi-product/delete/${id}`, 'DELETE');
        loadLedBoardPlugins(currentPage.ledBoardPlugin);
        
        // 如果在树形结构页面，刷新树形数据
        if (document.getElementById('tree-section').style.display !== 'none') {
            loadTreeData();
        }
        
        alert('删除成功！');
    } catch (error) {
        console.error('删除灯板插件失败:', error);
    }
}



// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    loadCircuitBoards();
    
    // 添加回车键搜索功能
    document.getElementById('circuitBoardCode').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchCircuitBoards();
    });
    document.getElementById('circuitBoardName').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchCircuitBoards();
    });
    
    document.getElementById('semiProductCode').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchSemiProducts();
    });
    document.getElementById('semiProductName').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchSemiProducts();
    });
    
    document.getElementById('ledBoardPluginCode').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchLedBoardPlugins();
    });
    document.getElementById('ledBoardPluginName').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchLedBoardPlugins();
    });
});