{
	"id": "d2c30f54-5687-4b40-8033-7a5caa4557dd",
	"name": "办学质量评价提示模板",
	"desc": "根据学校背景信息，以及指标的相关内容，对学校的办学提供改进建议",
	"field": "教育领域",
	"steps": [
		{
		"id": "f9beaf35-91a3-474c-a40c-f44279c729e2",
		"name": "获得改进建议文本内容",
		"desc": "分析优势与不足之处，然后根据不足的方面生成改进建议的文本内容，并对文本内容的字数提出了限制",
		"prompts": [
			{
				"id": "8859150e-eee3-45d5-bffe-7a503498835b",
				"index": 0,
				"category": 0, // 0-预置提示内容，1-用户输入的语料，2-ai生成的文本作为语料
				"name": "指定角色，分配任务",
				"desc": "学校在某个指标中的表现，如果下面有许多二级指标，需要把二级指标的表现文本都带上。",
				"content": "现在你是一位教育专家，你要根据下面给出的有关某所中学的办学质量评价的文本内容，分析这所中学在那些方面表现的好，在哪些方面表现的差。然后综合分析这所中学表现差的方面，为这所中学写一段改进建议，改进建议的字数不要低于600字。",
				"relyid": "" // 如果category为2，则必须要求指relyid，说明这一项内容具体要应用的是哪个步骤得到的结果
			},
			{
				"id": "50243388-19db-4542-a7ff-668a1451003b",
				"index": 1,
				"category": 1, // 0-预置提示内容，1-用户输入的语料，2-ai生成的文本作为语料
				"name": "某中学在某一级指标上的排名等描述性文本内容",
				"desc": "学校在某个指标中的表现，如果下面有许多二级指标，需要把二级指标的表现文本都带上。",
				"content": "",
				"relyid": ""
			}
		],
		"needed": 1, // 0-不被后续步骤依赖，1-被后续步骤所依赖
	},
	{
		"id": "7d55f521-fd8c-42a0-a3b1-5c90645f382e",
		"name": "生成改进建议标题",
		"desc": "得到一条改进建议的标题",
		"prompts": [
			{
				"id": "9a944e49-f0ca-4bae-a2da-94d5c8c5a608",
				"index": 0,
				"category": 0,
				"name": "说明任务，即对文本进行摘要，进而得到标题",
				"desc": "说明任务",
				"content": "下面是专家为某所中学提出的改进建议文本内容，请将这些内容概括为若干个短句，例如深入推进国家课程校本化实施，提升领导团队课程领导力，促进学校内涵发展、提升领导团队的课程教学领导力，促进学校内涵发展等。",
				"relyid": "f9beaf35-91a3-474c-a40c-f44279c729e2"
			}, {
				"id": "c3b7c40e-4e45-4c8a-b736-3db2d67b8398",
				"index": 1,
				"category": 2,
				"name": "改进建议文本",
				"desc": "由上一步生成的改进建议文本内容",
				"content": "下面是专家为某所中学提出的改进建议文本内容，请将这些内容概括为若干个短句，例如深入推进国家课程校本化实施，提升领导团队课程领导力，促进学校内涵发展、提升领导团队的课程教学领导力，促进学校内涵发展等。",
				"relyid": "f9beaf35-91a3-474c-a40c-f44279c729e2"
			}
		],
		"needed": 0
	},
	{
		"id": "24361ecd-cb5b-43af-874e-4bc85a99eb50",
		"name": "将上位概念融入改进建议文本内容中",
		"desc": "将该指标内涵的文本部分融入到改进建议文本当中，使得改进建议当中包含部分阐述改进重要性的文本内容。",
		"prompts": [
			{
				"id": "3739de33-8ab7-4299-8f68-7135455e5165",
				"index": 0,
				"category": 0,
				"name": "派送任务",
				"desc": "某个一级指标以及下面的二级指标的内涵、意义描述文本。可以从政策文本中引用。",
				"content": "下面的文本内容一是对一些指标的重要性的阐释，文本内容二是某位专家为某所中学提出的办学改进建议。现在你要以文本内容二为主，从文本内容一中选择一些内容并融入到文本内容二中，形成一段不少于600字的文本，注意不要改变文本内容二的意思。文本内容一如下：",
				"relyid": ""
			},
			{
				"id": "d4e882f1-9357-44d7-979c-4aa7b0ee7dc5",
				"index": 1,
				"category": 1,
				"name": "阐释某项评价指标的内涵、意义的文本",
				"desc": "某个一级指标以及下面的二级指标的内涵、意义描述文本。可以从政策文本中引用。",
				"content": "",
				"relyid": ""
			},
			{
				"id": "64723a30-a0de-4276-8648-fd99101c9a7f",
				"index": 2,
				"category": 0,
				"name": "派送任务",
				"desc": "某个一级指标以及下面的二级指标的内涵、意义描述文本。可以从政策文本中引用。",
				"content": "文本内容二如下：",
				"relyid": ""
			},
			{
				"id": "d649f867-e6a1-4c10-8e65-ba4471fb181e",
				"index": 3,
				"category": 2,
				"name": "已经生成的改进建议文本",
				"desc": "由第一步生成的改进建议的文本内容",
				"content": "",
				"relyid": "f9beaf35-91a3-474c-a40c-f44279c729e2"
			},
		],
		"needed": 1
	},
	{
		"id": "690fcdf3-3ba1-45c1-8857-8c5464ff5c30",
		"name": "结合学校实际修改学校改进建议的文本内容",
		"desc": "将学校目前的真实表现情况文本部分融入到改进建议文本当中，使得改进建议文本当中包含对学校具体情况的分析语句。",
		"prompts": [
			{
				"id": "fc819f30-096d-46c8-923a-c083daafbace",
				"index": 0,
				"category": 0,
				"name": "派送任务",
				"desc": "告知模型应该完成的内容",
				"content": "下面的文本内容一是某所中学在实际的教学工作中的自我评价，文本内容二是一段为该中学在办学方面需要改进的内容提出的改进建议。现在你要根据文本内容一分析这所中学有哪些优势，然后将这些优势适当的加入到文本内容二中，使得文本内容二不仅仅包含了对这所中学表现的不好的方面的改进建议，还有对这所学校表现的好的方面的肯定。注意整合后的文本不要偏离文本内容二的意思，整合后的文本的字数不少于600字。文本内容一如下：",
				"relyid": ""
			}, 
			{
				"id": "92ee9198-7e11-4935-a768-12312341657d",
				"index": 1,
				"category": 1,
				"name": "学校实际办学表现的文本内容",
				"desc": "学校在各个方面的表现的文本描述",
				"content": "",
				"relyid": ""
			}, 
			{
				"id": "57eb044f-b1b2-441b-8370-cf2d8fc795cd",
				"index": 2,
				"category": 0,
				"name": "文本内容二提示文本",
				"desc": "",
				"content": "文本内容二如下：",
				"relyid": ""
			}, 
			{
				"id": "3e1d9150-326b-4fc2-af9b-1cd6a06fde88",
				"index": 3,
				"category": 2,
				"name": "融合了概念文本的改进建议内容",
				"desc": "由上一步得到的改进建议文本",
				"content": "",
				"relyid": "24361ecd-cb5b-43af-874e-4bc85a99eb50"
			}
		],
		"needed": 1
	},
	{
		"id": "b9fe5962-2fa6-4e9e-9114-ee25559745d9",
		"name": "调整改进建议文本的用词、语言风格、通顺程度等",
		"desc": "调整改进建议的用词，以及改进建议的语言风格等。",
		"prompts": [
			{
				"id": "2eca438c-52f8-460e-9ab2-1af05c6b9407",
				"index": 0,
				"category": 0,
				"name": "派送任务",
				"desc": "告知模型要调整文本内容",
				"content": "以下内容是某位专家对某个中学的办学提出的改进建议文本。你要对文本内容进行修改，在不改变文本原有的意思的前提下，确保修改后的文本内容中没有人称代词，保证文本的语气要温和、要体现[建议]而不是[批评]。",
				"relyid": ""
			},
			{
				"id": "369fb1e0-025a-4932-a694-d80e2d9fa059",
				"index": 1,
				"category": 2,
				"name": "修改了两次之后的改进建议文本内容",
				"desc": "融合了指标含义、概念，以及包含了学校实际办学情况的改进建议文本内容，由上一步得到",
				"content": "",
				"relyid": "690fcdf3-3ba1-45c1-8857-8c5464ff5c30"
			}
		],
		"needed": 0
	}
],
	"creator": "10001",
	"create_time": "1679809458469"
}